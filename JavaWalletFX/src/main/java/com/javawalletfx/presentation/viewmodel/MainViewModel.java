package com.javawalletfx.presentation.viewmodel;

import com.javawallet.application.command.CommandInvoker;
import com.javawallet.application.manager.FinanceManager;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public class MainViewModel {

    private final FinanceManager financeManager;
    private final CommandInvoker commandInvoker;

    // Proprietà osservabili per la UI
    private final ObjectProperty<Wallet> currentWallet = new SimpleObjectProperty<>();
    private final StringProperty balanceText = new SimpleStringProperty("0.00");

    public MainViewModel(FinanceManager financeManager, CommandInvoker commandInvoker) {
        this.financeManager = financeManager;
        this.commandInvoker = commandInvoker;

        // Quando il wallet cambia, aggiorniamo automaticamente il testo del saldo
        currentWallet.addListener((obs, oldW, newW) -> updateBalanceText(newW));
    }

    // --- Metodi chiamati dal Controller ---

    public void loadWallet(UUID walletId) {
        // Recupera dal DB tramite il Manager
        financeManager.getWallet(walletId).ifPresent(this::setWallet);
    }

    public void undo() {
        commandInvoker.undo();
        refreshState(); // Forza il ricaricamento della UI dopo l'undo
    }

    public void redo() {
        commandInvoker.redo();
        refreshState();
    }

    public void addTransaction(Transaction t) {
        if (currentWallet.get() != null) {
            commandInvoker.addTransaction(currentWallet.get(), t);
            refreshState();
        }
    }

    // --- Helpers ---

    private void refreshState() {
        // Poiché il CommandInvoker modifica l'oggetto in memoria,
        // dobbiamo notificare la UI che l'oggetto è "sporco/cambiato".
        // Un trucco rapido in JavaFX è risettare lo stesso oggetto o fare fireValueChanged.
        // Qui per semplicità ricarichiamo (in una app reale useremmo eventi più fini).
        Wallet w = currentWallet.get();
        if (w != null) {
            // "Tocchiamo" la property per scatenare i listener del controller
            currentWallet.set(null);
            currentWallet.set(w);
        }
    }

    private void setWallet(Wallet w) {
        currentWallet.set(w);
    }

    private void updateBalanceText(Wallet w) {
        if (w != null) {
            balanceText.set(w.getBalance().toString());
        } else {
            balanceText.set("---");
        }
    }

    // --- Getters for Properties (JavaFX Convention) ---

    public ObjectProperty<Wallet> currentWalletProperty() {
        return currentWallet;
    }

    public StringProperty balanceTextProperty() {
        return balanceText;
    }

    // In MainViewModel.java

    public void createWallet(String name) {
        // Crea un wallet di default (es. Checking Account, 0 EUR)
        Wallet w = com.javawallet.domain.factory.WalletFactory.create(
                name,
                com.javawallet.domain.model.WalletType.CHECKINGACCOUNT,
                com.javawallet.domain.model.Money.zero("EUR")
        );

        // Salva su DB tramite CommandInvoker così supportiamo UNDO
        commandInvoker.createWallet(financeManager, w);

        // Lo impostiamo come corrente
        setWallet(w);
    }
}
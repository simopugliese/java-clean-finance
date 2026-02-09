package com.javawalletfx.presentation.viewmodel;

import com.javawallet.application.command.CommandInvoker;
import com.javawallet.application.manager.FinanceManager;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.Money;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.model.WalletType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class MainViewModel {

    private final FinanceManager financeManager;
    private final CommandInvoker commandInvoker;

    // --- Proprietà Osservabili per la UI ---

    // Il wallet attualmente selezionato
    private final ObjectProperty<Wallet> currentWallet = new SimpleObjectProperty<>();

    // Testo del saldo (es. "100.00 EUR")
    private final StringProperty balanceText = new SimpleStringProperty("0.00");

    // Liste osservabili per ListView e TableView
    private final ObservableList<Wallet> walletsList = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();

    public MainViewModel(FinanceManager financeManager, CommandInvoker commandInvoker) {
        this.financeManager = financeManager;
        this.commandInvoker = commandInvoker;

        // Listener: Quando cambia il wallet selezionato (currentWallet)...
        currentWallet.addListener((obs, oldWallet, newWallet) -> {
            refreshTransactionsList(newWallet);
            updateBalanceText(newWallet);
        });

        // TODO: Se avessi un metodo financeManager.getAllWallets(), lo chiameresti qui per popolare walletsList all'avvio.
        // Per ora la lista parte vuota e si riempie creando nuovi wallet.
    }

    // --- Getters per il Binding (usati dal Controller) ---

    public ObservableList<Wallet> getWallets() {
        return walletsList;
    }

    public ObservableList<Transaction> getTransactions() {
        return transactionsList;
    }

    public ObjectProperty<Wallet> currentWalletProperty() {
        return currentWallet;
    }

    public StringProperty balanceTextProperty() {
        return balanceText;
    }

    // --- Azioni (Chiamate dal Controller) ---

    public void selectWallet(Wallet w) {
        currentWallet.set(w);
    }

    public void createWallet(String name) {
        // 1. Crea il Wallet usando la Factory del dominio
        Wallet w = WalletFactory.create(
                name,
                WalletType.CHECKINGACCOUNT, // Default per semplicità
                Money.zero("EUR")
        );

        // 2. Esegui il comando (così supportiamo l'Undo della creazione)
        commandInvoker.createWallet(financeManager, w);

        // 3. Aggiorna la UI
        walletsList.add(w);
        selectWallet(w);
    }

    public void addTransaction(Transaction t) {
        if (currentWallet.get() != null) {
            // Esegue il comando sul dominio
            commandInvoker.addTransaction(currentWallet.get(), t);

            // Aggiorna la vista
            refreshState();
        }
    }

    public void undo() {
        commandInvoker.undo(); //
        refreshState();
    }

    public void redo() {
        commandInvoker.redo(); //
        refreshState();
    }

    // --- Metodi di Aggiornamento Stato UI ---

    /**
     * Ricarica tutti i dati visibili in base allo stato attuale del dominio.
     * Chiamato dopo ogni operazione che modifica i dati (Add, Undo, Redo).
     */
    private void refreshState() {
        Wallet w = currentWallet.get();
        if (w != null) {
            // Aggiorna la lista transazioni
            refreshTransactionsList(w);
            // Aggiorna il saldo
            updateBalanceText(w);

            // Hack per notificare la ListView dei wallet che il saldo/stato interno potrebbe essere cambiato
            // In un'app reale useremmo proprietà osservabili dentro Wallet o un Extractor.
            int index = walletsList.indexOf(w);
            if (index >= 0) {
                walletsList.set(index, w);
            }
        }
    }

    private void refreshTransactionsList(Wallet w) {
        transactionsList.clear();
        if (w != null) {
            // Prende le transazioni dal dominio e le mette nella lista osservabile
            transactionsList.addAll(w.getTransactions());
        }
    }

    private void updateBalanceText(Wallet w) {
        if (w != null) {
            balanceText.set(w.getBalance().toString());
        } else {
            balanceText.set("---");
        }
    }

    // Metodo opzionale se volessi caricare un wallet specifico da DB
    public void loadWallet(UUID walletId) {
        financeManager.getWallet(walletId).ifPresent(w -> {
            if (!walletsList.contains(w)) {
                walletsList.add(w);
            }
            selectWallet(w);
        });
    }
}
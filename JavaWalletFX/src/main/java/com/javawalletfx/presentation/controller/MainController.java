package com.javawalletfx.presentation.controller;

import com.javawallet.domain.model.Money;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.TransactionBuilder;
import com.javawallet.domain.model.TransactionType;
import com.javawallet.domain.model.Wallet;
import com.javawalletfx.presentation.viewmodel.MainViewModel;
import com.javawalletfx.presentation.visitor.TreeItemVisitor;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class MainController {

    @FXML private TreeView<String> financeTree;
    @FXML private Label balanceLabel;
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;

    private MainViewModel viewModel;

    /**
     * Metodo di inizializzazione chiamato da JavaWalletApp.
     * Qui colleghiamo il ViewModel alla View.
     */
    public void init(MainViewModel viewModel) {
        this.viewModel = viewModel;

        // 1. Binding del testo del saldo
        balanceLabel.textProperty().bind(viewModel.balanceTextProperty());

        // 2. Listener sul Wallet corrente: se cambia, ridisegniamo l'albero
        viewModel.currentWalletProperty().addListener((obs, oldWallet, newWallet) -> {
            refreshTreeView(newWallet);
        });

        // Se c'è già un wallet caricato all'avvio, visualizzalo subito
        if (viewModel.currentWalletProperty().get() != null) {
            refreshTreeView(viewModel.currentWalletProperty().get());
        }
    }

    /**
     * Usa il Visitor per costruire l'albero grafico a partire dal dominio.
     */
    private void refreshTreeView(Wallet wallet) {
        if (wallet == null) {
            financeTree.setRoot(null);
            return;
        }

        // --- QUI USIAMO IL VISITOR CHE ABBIAMO CREATO ---
        TreeItemVisitor visitor = new TreeItemVisitor();

        // Grazie alla modifica fatta nel backend, questo chiama visit(wallet)
        // e poi siamo NOI nel visitor a decidere di visitare i figli.
        wallet.accept(visitor);

        financeTree.setRoot(visitor.getResult());
    }

    // --- EVENT HANDLERS (Definiti nell'FXML) ---

    @FXML
    public void onUndo() {
        viewModel.undo();
    }

    @FXML
    public void onRedo() {
        viewModel.redo();
    }

    @FXML
    public void onNewWallet() {
        // TODO: In futuro apriremo una Dialog complessa. Per ora usiamo un input semplice.
        TextInputDialog dialog = new TextInputDialog("My Wallet");
        dialog.setTitle("New Wallet");
        dialog.setHeaderText("Create a new Wallet");
        dialog.setContentText("Wallet Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            // Per semplicità creiamo un wallet di default.
            // In una app reale chiederemmo anche il tipo e il saldo iniziale.
            viewModel.createWallet(name);
        });
    }

    @FXML
    public void onAddTransaction() {
        if (viewModel.currentWalletProperty().get() == null) {
            showAlert("Error", "No wallet selected!");
            return;
        }

        // TODO: Qui andrebbe una Dialog con DatePicker, ComboBox per Categoria, ecc.
        // Per ora facciamo una simulazione hardcoded o un input semplice per testare.
        TextInputDialog dialog = new TextInputDialog("100.00");
        dialog.setTitle("Add Transaction");
        dialog.setHeaderText("Deposit Money (Test)");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);

                // Costruiamo la transazione
                Transaction t = new TransactionBuilder(
                        Money.of(amount, "EUR"),
                        TransactionType.DEPOSIT, // Default a deposito per test
                        LocalDateTime.now()
                ).withNote("Test Transaction").build();

                viewModel.addTransaction(t);

            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number");
            } catch (Exception e) {
                showAlert("Error", e.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.javawalletfx.presentation.controller;

import com.javawallet.domain.model.*;
import com.javawalletfx.presentation.viewmodel.MainViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class MainController {

    // --- FXML INJECTIONS ---
    @FXML private ListView<Wallet> walletList;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colNote;

    @FXML private Label balanceLabel;
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;

    private MainViewModel viewModel;

    public void init(MainViewModel viewModel) {
        this.viewModel = viewModel;

        // 1. Binding Generali
        balanceLabel.textProperty().bind(viewModel.balanceTextProperty());

        // 2. Setup Lista Wallet (Master)
        // Nota: Assicurati che il ViewModel esponga getWallets() come ObservableList
        walletList.setItems(viewModel.getWallets());

        // Custom Cell Factory per mostrare il nome del wallet nella lista
        walletList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Wallet item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getType() + ")");
            }
        });

        // Listener Selezione: Quando clicco un wallet, aggiorno il ViewModel
        walletList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.selectWallet(newVal);
            }
        });

        // 3. Setup Tabella Transazioni (Detail)
        setupTransactionTable();

        // Binding items: La tabella ascolta la lista transazioni del wallet corrente
        transactionTable.setItems(viewModel.getTransactions());

        // Seleziona il primo wallet se esiste
        if (!viewModel.getWallets().isEmpty()) {
            walletList.getSelectionModel().selectFirst();
        }
    }

    private void setupTransactionTable() {
        // Data: Formattazione semplice
        colDate.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDate().toLocalDate().toString()));

        // Categoria: Nome della categoria o "N/A"
        colCategory.setCellValueFactory(cell -> {
            Category cat = cell.getValue().getCategory();
            return new SimpleStringProperty(cat != null ? cat.getName() : "N/A");
        });

        // Tipo: Enum to String
        colType.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getType().toString()));

        // Importo: Estrazione dal Value Object Money
        colAmount.setCellValueFactory(cell -> {
            Money m = cell.getValue().getMoney();
            return new SimpleStringProperty(m.getAmount() + " " + m.getCurrency());
        });

        // Note
        colNote.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNote()));

        // Colora le righe: Rosso per uscite, Verde per entrate (Opzionale)
        transactionTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getType() == TransactionType.WITHDRAW) {
                    setStyle("-fx-text-background-color: #d32f2f;"); // Rosso scuro
                } else if (item.getType() == TransactionType.DEPOSIT) {
                    setStyle("-fx-text-background-color: #388e3c;"); // Verde scuro
                } else {
                    setStyle("");
                }
            }
        });
    }

    // --- ACTIONS ---

    @FXML
    public void onNewWallet() {
        // Dialogo semplice per nome wallet
        TextInputDialog dialog = new TextInputDialog("My Wallet");
        dialog.setTitle("New Wallet");
        dialog.setHeaderText("Crea un nuovo Wallet");
        dialog.setContentText("Nome:");

        dialog.showAndWait().ifPresent(name -> {
            viewModel.createWallet(name);
            // Forza refresh o selezione
            walletList.getSelectionModel().selectLast();
        });
    }

    @FXML
    public void onAddTransaction() {
        if (viewModel.currentWalletProperty().get() == null) {
            showAlert(Alert.AlertType.WARNING, "Nessun Wallet Selezionato", "Seleziona un wallet prima di aggiungere una transazione.");
            return;
        }

        showAddTransactionDialog();
    }

    @FXML
    public void onUndo() {
        viewModel.undo();
        transactionTable.refresh(); // Refresh visivo forzato
    }

    @FXML
    public void onRedo() {
        viewModel.redo();
        transactionTable.refresh();
    }

    // --- DIALOGS & HELPERS ---

    private void showAddTransactionDialog() {
        // Creazione programmatica del Dialog (per evitare file FXML extra ora)
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Nuova Transazione");
        dialog.setHeaderText("Inserisci dettagli transazione");

        // Buttons
        ButtonType loginButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Layout Form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");

        ComboBox<TransactionType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(TransactionType.values()));
        typeCombo.setValue(TransactionType.WITHDRAW); // Default

        TextField noteField = new TextField();
        noteField.setPromptText("Spesa supermercato...");

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());

        grid.add(new Label("Importo:"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Data:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Nota:"), 0, 3);
        grid.add(noteField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convertitore Risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    BigDecimal amount = new BigDecimal(amountField.getText());
                    TransactionType type = typeCombo.getValue();
                    LocalDateTime date = datePicker.getValue().atStartOfDay();

                    // TODO: Gestire selezione Categoria Reale. Qui usiamo null o mock per ora.
                    Category dummyCategory = null;

                    return new TransactionBuilder(Money.of(amount, "EUR"), type, date)
                            .withCategory(dummyCategory)
                            .withNote(noteField.getText())
                            .build();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Dati Invalidi", "Controlla l'importo inserito.");
                    return null;
                }
            }
            return null;
        });

        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(t -> viewModel.addTransaction(t));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
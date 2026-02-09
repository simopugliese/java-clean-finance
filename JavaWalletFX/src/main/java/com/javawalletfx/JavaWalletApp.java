package com.javawalletfx;

import com.javawallet.application.command.CommandInvoker;
import com.javawallet.application.manager.FinanceManager;
import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.Money;
import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.model.WalletType;
import com.javawallet.infrastructure.persistence.MariaDBCategoryPersistence;
import com.javawallet.infrastructure.persistence.MariaDBWalletPersistence;
import com.javawalletfx.presentation.controller.MainController;
import com.javawalletfx.presentation.viewmodel.MainViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class JavaWalletApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Setup Infrastructure & Application Layer (Manual DI)
        // Nota: Assicurati che il DB sia attivo o usa stub per testare
        IWalletRepository walletRepo = new MariaDBWalletPersistence();
        ICategoryRepository categoryRepo = new MariaDBCategoryPersistence();
        WalletFactory walletFactory = new WalletFactory();

        FinanceManager financeManager = new FinanceManager(walletRepo, categoryRepo, walletFactory);
        CommandInvoker commandInvoker = new CommandInvoker();

        // DATI DI TEST (Opzionale: crea un wallet se il DB è vuoto per non vedere schermo bianco)
        // Wallet testWallet = walletFactory.create("Main Wallet", WalletType.CheckingAccount, Money.of(BigDecimal.ZERO, "EUR"));
        // financeManager.addWallet(testWallet);

        // 2. Setup Presentation Layer
        MainViewModel viewModel = new MainViewModel(financeManager, commandInvoker);

        // 3. Load View
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javawalletfx/presentation/view/MainView.fxml"));
        Parent root = loader.load();

        // 4. Inject ViewModel into Controller
        MainController controller = loader.getController();
        controller.init(viewModel);

        // 5. Opzionale: Carica un wallet di default all'avvio se ne hai uno su DB
        // viewModel.loadWallet(UUID.fromString("..."));

        // 6. Show Stage
        Scene scene = new Scene(root);
        stage.setTitle("Java Clean Finance");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
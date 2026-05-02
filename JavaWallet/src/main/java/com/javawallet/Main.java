package com.javawallet;

import com.javawallet.application.command.CommandInvoker;
import com.javawallet.application.manager.FinanceManager;
import com.javawallet.application.ports.MariaDbCategoryRepository;
import com.javawallet.application.ports.MariaDbWalletRepository;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.*;
import com.javawallet.domain.visitor.ReportCLI;
import com.javawallet.infrastructure.persistence.IPersistenceContext;
import com.javawallet.infrastructure.persistence.MariaDbConnectionManager;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        // =======================================================
        // 1. BOOTSTRAP — assemble tutte le dipendenze
        // =======================================================
        MariaDbConnectionManager connectionManager = new MariaDbConnectionManager();
        connectionManager.initializeDatabase();

        WalletFactory walletFactory = new WalletFactory();
        MariaDbWalletRepository walletRepository = new MariaDbWalletRepository(connectionManager);
        MariaDbCategoryRepository categoryRepository = new MariaDbCategoryRepository(connectionManager);
        IPersistenceContext persistenceContext = new IPersistenceContext(walletRepository, categoryRepository);

        ReportCLI reportCLI = new ReportCLI();
        CommandInvoker commandInvoker = new CommandInvoker();

        FinanceManager financeManager = new FinanceManager(
                walletFactory,
                reportCLI,
                commandInvoker,
                persistenceContext
        );

        // =======================================================
        // 2. DEMO INTERATTIVA (menu minimo)
        // =======================================================
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n═══════════════════════════════════");
            System.out.println("  JAVA WALLET — Finance Manager");
            System.out.println("═══════════════════════════════════");
            System.out.println("1. Crea un portafoglio");
            System.out.println("2. Crea una categoria");
            System.out.println("3. Aggiungi una transazione");
            System.out.println("4. Mostra portafogli");
            System.out.println("5. Mostra categorie");
            System.out.println("6. Genera report portafoglio");
            System.out.println("7. Undo");
            System.out.println("8. Redo");
            System.out.println("0. Esci");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createWallet(financeManager, scanner);
                    case "2" -> createCategory(financeManager, scanner);
                    case "3" -> createTransaction(financeManager, scanner);
                    case "4" -> showWallets(financeManager, scanner);
                    case "5" -> showCategories(financeManager);
                    case "6" -> generateReport(financeManager, scanner);
                    case "7" -> { financeManager.undo(); System.out.println("✓ Undo eseguito"); }
                    case "8" -> { financeManager.redo(); System.out.println("✓ Redo eseguito"); }
                    case "0" -> {
                        System.out.println("Arrivederci!");
                        System.exit(0);
                    }
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }

    private static void createWallet(FinanceManager fm, Scanner scanner) {
        System.out.print("Nome portafoglio: ");
        String name = scanner.nextLine().trim();

        System.out.println("Tipo (1=DEBITCARD, 2=CHECKINGACCOUNT, 3=CREDITCARD): ");
        String typeChoice = scanner.nextLine().trim();
        WalletType type = switch (typeChoice) {
            case "1" -> WalletType.DEBITCARD;
            case "2" -> WalletType.CHECKINGACCOUNT;
            case "3" -> WalletType.CREDITCARD;
            default -> throw new IllegalArgumentException("Tipo non valido");
        };

        System.out.print("Saldo iniziale (es. 1000.00): ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Valuta (es. EUR, USD): ");
        String currency = scanner.nextLine().trim().toUpperCase();

        Money balance = Money.of(amount, currency);
        Wallet wallet = fm.createWallet(name, type, balance);

        System.out.println("✓ Portafoglio creato: " + wallet.getId());
    }

    private static void createCategory(FinanceManager fm, Scanner scanner) {
        System.out.print("Nome categoria: ");
        String name = scanner.nextLine().trim();
        fm.createCategory(name);
        System.out.println("✓ Categoria creata");
    }

    private static void createTransaction(FinanceManager fm, Scanner scanner) {
        System.out.print("ID portafoglio: ");
        String walletIdStr = scanner.nextLine().trim();
        UUID walletId = UUID.fromString(walletIdStr);

        System.out.println("Tipo (1=DEPOSIT, 2=WITHDRAWAL, 3=TRANSFER): ");
        String typeChoice = scanner.nextLine().trim();
        TransactionType type = switch (typeChoice) {
            case "1" -> TransactionType.DEPOSIT;
            case "2" -> TransactionType.WITHDRAWAL;
            case "3" -> TransactionType.TRANSFER;
            default -> throw new IllegalArgumentException("Tipo non valido");
        };

        System.out.print("Importo (es. 150.00): ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Valuta (es. EUR): ");
        String currency = scanner.nextLine().trim().toUpperCase();

        Money money = Money.of(amount, currency);

        System.out.print("Categoria (premere Invio per saltare): ");
        String catInput = scanner.nextLine().trim();
        if (!catInput.isEmpty()) {
            Category cat = fm.getCategories().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(catInput))
                    .findFirst()
                    .orElse(null);
            fm.createTransaction(walletId, money, type, cat);
        } else {
            fm.createTransaction(walletId, money, type);
        }

        System.out.println("✓ Transazione creata");
    }

    private static void showWallets(FinanceManager fm, Scanner scanner) {
        System.out.print("ID portafoglio da visualizzare (oppure 0 per tornare indietro): ");
        String input = scanner.nextLine().trim();
        if ("0".equals(input)) return;

        Wallet wallet = fm.getWallet(UUID.fromString(input));
        System.out.println("Nome: " + wallet.getName());
        System.out.println("Tipo: " + wallet.getType());
        System.out.println("Saldo: " + wallet.getBalance());
        System.out.println("Transazioni:");
        wallet.getTransactions().forEach(t ->
                System.out.println("  ▸ " + t.getType() + " " + t.getMoney() + " — " + t.getDate())
        );
    }

    private static void showCategories(FinanceManager fm) {
        var categories = fm.getCategories();
        if (categories.isEmpty()) {
            System.out.println("Nessuna categoria presente.");
            return;
        }
        categories.forEach(c -> System.out.println("▸ " + c.getName()));
    }

    private static void generateReport(FinanceManager fm, Scanner scanner) {
        System.out.print("ID portafoglio da reportare: ");
        String walletIdStr = scanner.nextLine().trim();
        UUID walletId = UUID.fromString(walletIdStr);

        Wallet wallet = fm.getWallet(walletId);
        fm.generateReport(wallet);
    }
}

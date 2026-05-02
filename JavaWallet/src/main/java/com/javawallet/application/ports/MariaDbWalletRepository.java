package com.javawallet.application.ports;

import com.javawallet.domain.model.*;
import com.javawallet.domain.strategy.IRuleStrategy;
import com.javawallet.domain.strategy.MaxWithdraw;
import com.javawallet.domain.strategy.NegativeBalanceNotAllowed;
import com.javawallet.infrastructure.persistence.MariaDbConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MariaDbWalletRepository implements IWalletRepository {

    private final MariaDbConnectionManager connectionManager;

    public MariaDbWalletRepository(MariaDbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    // ---------------------------------------------------------------
    // UPSERT WALLET (insert or update wallet + strategies + transactions)
    // ---------------------------------------------------------------
    @Override
    public void upsertWallet(Wallet w) {
        String upsertWalletSql =
                "INSERT INTO wallets (id, name, type, balance_amount, balance_currency) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), type = VALUES(type), " +
                        "balance_amount = VALUES(balance_amount), balance_currency = VALUES(balance_currency)";

        String deleteStrategiesSql = "DELETE FROM wallet_rule_strategies WHERE wallet_id = ?";
        String insertStrategySql =
                "INSERT INTO wallet_rule_strategies (wallet_id, strategy_class, max_amount) VALUES (?, ?, ?)";

        String upsertTransactionSql =
                "INSERT INTO transactions (id, wallet_id, category_id, type, amount, currency, date, note) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE category_id = VALUES(category_id), type = VALUES(type), " +
                        "amount = VALUES(amount), currency = VALUES(currency), date = VALUES(date), note = VALUES(note)";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psWallet = conn.prepareStatement(upsertWalletSql);
                 PreparedStatement psDeleteStrats = conn.prepareStatement(deleteStrategiesSql);
                 PreparedStatement psInsertStrat = conn.prepareStatement(insertStrategySql);
                 PreparedStatement psTransaction = conn.prepareStatement(upsertTransactionSql)) {

                // --- Upsert wallet ---
                psWallet.setString(1, w.getId().toString());
                psWallet.setString(2, w.getName());
                psWallet.setString(3, w.getType().name());
                psWallet.setBigDecimal(4, w.getBalance().getAmount());
                psWallet.setString(5, w.getBalance().getCurrency());
                psWallet.executeUpdate();

                // --- Replace strategies ---
                psDeleteStrats.setString(1, w.getId().toString());
                psDeleteStrats.executeUpdate();

                for (IRuleStrategy strategy : w.getRuleStrategy()) {
                    psInsertStrat.setString(1, w.getId().toString());
                    psInsertStrat.setString(2, strategy.getClass().getName());
                    if (strategy instanceof MaxWithdraw mw) {
                        psInsertStrat.setBigDecimal(3, mw.getMaxAmount());
                    } else {
                        psInsertStrat.setBigDecimal(3, null);
                    }
                    psInsertStrat.addBatch();
                }
                psInsertStrat.executeBatch();

                // --- Upsert transactions ---
                for (Transaction t : w.getTransactions()) {
                    psTransaction.setString(1, t.getId().toString());
                    psTransaction.setString(2, w.getId().toString());
                    psTransaction.setString(3, t.getCategory() != null ? t.getCategory().getId().toString() : null);
                    psTransaction.setString(4, t.getType().name());
                    psTransaction.setBigDecimal(5, t.getMoney().getAmount());
                    psTransaction.setString(6, t.getMoney().getCurrency());
                    psTransaction.setTimestamp(7, Timestamp.valueOf(t.getDate()));
                    psTransaction.setString(8, t.getNote());
                    psTransaction.addBatch();
                }
                psTransaction.executeBatch();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Failed to upsert wallet " + w.getId(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Connection error while upserting wallet " + w.getId(), e);
        }
    }

    // ---------------------------------------------------------------
    // LOAD ALL WALLETS
    // ---------------------------------------------------------------
    @Override
    public Collection<Wallet> loadWallets() {
        String sql = "SELECT id, name, type, balance_amount, balance_currency FROM wallets";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Wallet> wallets = new ArrayList<>();
            while (rs.next()) {
                wallets.add(buildWallet(conn, rs));
            }
            return wallets;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load wallets", e);
        }
    }

    // ---------------------------------------------------------------
    // GET WALLET BY UUID
    // ---------------------------------------------------------------
    @Override
    public Optional<Wallet> getWalletByUUID(UUID id) {
        String sql = "SELECT id, name, type, balance_amount, balance_currency FROM wallets WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildWallet(conn, rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get wallet by UUID " + id, e);
        }
    }

    // ---------------------------------------------------------------
    // REMOVE WALLET (cascade deletes strategies and transactions)
    // ---------------------------------------------------------------
    @Override
    public void removeWallet(UUID id) {
        String sql = "DELETE FROM wallets WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove wallet " + id, e);
        }
    }

    // ---------------------------------------------------------------
    // LOAD TRANSACTIONS BY WALLET
    // ---------------------------------------------------------------
    @Override
    public Collection<Transaction> loadByWallet(UUID id) {
        String sql = "SELECT t.id, t.category_id, t.type, t.amount, t.currency, t.date, t.note, " +
                "       c.name AS category_name, c.parent_id AS category_parent_id " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.wallet_id = ? " +
                "ORDER BY t.date ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return extractTransactions(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions for wallet " + id, e);
        }
    }

    // ---------------------------------------------------------------
    // LOAD TRANSACTIONS BY PERIOD (all wallets)
    // ---------------------------------------------------------------
    @Override
    public Collection<Transaction> loadByPeriod(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT t.id, t.category_id, t.type, t.amount, t.currency, t.date, t.note, " +
                "       c.name AS category_name, c.parent_id AS category_parent_id " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.date BETWEEN ? AND ? " +
                "ORDER BY t.date ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return extractTransactions(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions by period", e);
        }
    }

    // ---------------------------------------------------------------
    // LOAD TRANSACTIONS BY WALLET + PERIOD
    // ---------------------------------------------------------------
    @Override
    public Collection<Transaction> loadByWalletAndPeriod(UUID walletId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT t.id, t.category_id, t.type, t.amount, t.currency, t.date, t.note, " +
                "       c.name AS category_name, c.parent_id AS category_parent_id " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.wallet_id = ? AND t.date BETWEEN ? AND ? " +
                "ORDER BY t.date ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, walletId.toString());
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return extractTransactions(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions for wallet " + walletId + " by period", e);
        }
    }

    // ---------------------------------------------------------------
    // REMOVE TRANSACTION
    // ---------------------------------------------------------------
    @Override
    public boolean removeTransaction(UUID walletId, UUID transactionID) {
        String sql = "DELETE FROM transactions WHERE id = ? AND wallet_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionID.toString());
            ps.setString(2, walletId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove transaction " + transactionID, e);
        }
    }

    // =================================================================
    //  PRIVATE HELPERS
    // =================================================================

    /**
     * Build a Wallet domain object from a ResultSet row (which already points to a wallet row).
     * Also loads its rule strategies and transactions from the DB.
     */
    private Wallet buildWallet(Connection conn, ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String name = rs.getString("name");
        WalletType type = WalletType.valueOf(rs.getString("type"));
        Money balance = Money.of(rs.getBigDecimal("balance_amount"), rs.getString("balance_currency"));

        // Load strategies for this wallet
        Collection<IRuleStrategy> strategies = loadStrategies(conn, id);

        // Load transactions for this wallet (same connection)
        Collection<Transaction> transactions = loadTransactionsByWallet(conn, id);

        Wallet wallet = new Wallet(name, type, balance, strategies, transactions);
        // Wallet constructor generates a random UUID; override with the stored one
        setWalletId(wallet, id);
        return wallet;
    }

    /**
     * Load rule strategies from the database for a given wallet.
     */
    private Collection<IRuleStrategy> loadStrategies(Connection conn, UUID walletId) throws SQLException {
        String sql = "SELECT strategy_class, max_amount FROM wallet_rule_strategies WHERE wallet_id = ?";
        List<IRuleStrategy> strategies = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, walletId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String className = rs.getString("strategy_class");
                    IRuleStrategy strategy = instantiateStrategy(className, rs.getBigDecimal("max_amount"));
                    if (strategy != null) {
                        strategies.add(strategy);
                    }
                }
            }
        }
        return strategies;
    }

    /**
     * Instantiate a strategy by its fully-qualified class name.
     */
    private IRuleStrategy instantiateStrategy(String className, BigDecimal maxAmount) {
        try {
            if (className.equals(NegativeBalanceNotAllowed.class.getName())) {
                return new NegativeBalanceNotAllowed();
            } else if (className.equals(MaxWithdraw.class.getName())) {
                BigDecimal amount = (maxAmount != null) ? maxAmount : new BigDecimal("1000");
                return new MaxWithdraw(amount);
            }
            // If unknown strategy, try reflection as fallback
            Class<?> clazz = Class.forName(className);
            return (IRuleStrategy) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Warning: could not instantiate strategy " + className + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Load transactions for a given wallet, reusing an existing connection.
     */
    private Collection<Transaction> loadTransactionsByWallet(Connection conn, UUID walletId) throws SQLException {
        String sql = "SELECT t.id, t.category_id, t.type, t.amount, t.currency, t.date, t.note, " +
                "       c.name AS category_name, c.parent_id AS category_parent_id " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.wallet_id = ? " +
                "ORDER BY t.date ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, walletId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return extractTransactions(rs);
            }
        }
    }

    /**
     * Extract Transaction objects from a ResultSet that already contains the joined category info.
     */
    private Collection<Transaction> extractTransactions(ResultSet rs) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        while (rs.next()) {
            transactions.add(buildTransaction(rs));
        }
        return transactions;
    }

    /**
     * Build a single Transaction domain object from the current ResultSet row.
     */
    private Transaction buildTransaction(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        TransactionType type = TransactionType.valueOf(rs.getString("type"));
        Money money = Money.of(rs.getBigDecimal("amount"), rs.getString("currency"));
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        String note = rs.getString("note");

        // Build category if present
        String categoryIdStr = rs.getString("category_id");
        Category category = null;
        if (categoryIdStr != null) {
            String categoryName = rs.getString("category_name");
            category = new Category(categoryName != null ? categoryName : "Unknown");
            // Use reflection to set the ID so it matches the stored UUID
            setCategoryId(category, UUID.fromString(categoryIdStr));
        }

        // Use the TransactionBuilder to create the transaction
        TransactionBuilder builder = new TransactionBuilder(money, type)
                .withCategory(category)
                .withDate(date)
                .withNote(note);

        Transaction tx = builder.build();
        // Override the auto-generated ID with the one from the database
        setTransactionId(tx, id);

        return tx;
    }

    /**
     * Use reflection to set the ID of a Category (since it has no public setter).
     */
    private void setCategoryId(Category category, UUID id) {
        try {
            java.lang.reflect.Field idField = Category.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(category, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set category ID", e);
        }
    }

    /**
     * Use reflection to set the ID of a Transaction (since the constructor auto-generates it).
     */
    private void setTransactionId(Transaction transaction, UUID id) {
        try {
            java.lang.reflect.Field idField = Transaction.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(transaction, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set transaction ID", e);
        }
    }

    /**
     * Use reflection to set the ID of a Wallet (since the constructor auto-generates it).
     */
    private void setWalletId(Wallet wallet, UUID id) {
        try {
            java.lang.reflect.Field idField = Wallet.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(wallet, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set wallet ID", e);
        }
    }
}

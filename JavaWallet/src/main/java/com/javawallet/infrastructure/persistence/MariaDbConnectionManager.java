package com.javawallet.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDbConnectionManager {

    private static final String PROP_PREFIX = "maria.db.";
    private static final String ENV_PREFIX = "MARIA_DB_";

    private final String url;
    private final String user;
    private final String password;

    public MariaDbConnectionManager() {
        this.url = resolveConfig("url", "URL", "database URL");
        this.user = resolveConfig("user", "USER", "database user");
        this.password = resolveConfig("password", "PASSWORD", "database password");
    }

    public MariaDbConnectionManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private static String resolveConfig(String propSuffix, String envSuffix, String description) {
        String value = System.getProperty(PROP_PREFIX + propSuffix);
        if (value != null && !value.isBlank()) return value;

        value = System.getenv(ENV_PREFIX + envSuffix);
        if (value != null && !value.isBlank()) return value;

        throw new IllegalStateException(
                "Missing " + description + ": set system property '" + PROP_PREFIX + propSuffix +
                "' or environment variable '" + ENV_PREFIX + envSuffix + "'"
        );
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS wallets (" +
                            "  id VARCHAR(36) PRIMARY KEY," +
                            "  name VARCHAR(255) NOT NULL," +
                            "  type VARCHAR(50) NOT NULL," +
                            "  balance_amount DECIMAL(19,2) NOT NULL," +
                            "  balance_currency VARCHAR(3) NOT NULL" +
                            ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS categories (" +
                            "  id VARCHAR(36) PRIMARY KEY," +
                            "  name VARCHAR(255) NOT NULL," +
                            "  parent_id VARCHAR(36) NULL," +
                            "  FOREIGN KEY (parent_id) REFERENCES categories(id)" +
                            ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS transactions (" +
                            "  id VARCHAR(36) PRIMARY KEY," +
                            "  wallet_id VARCHAR(36) NOT NULL," +
                            "  category_id VARCHAR(36) NULL," +
                            "  type VARCHAR(50) NOT NULL," +
                            "  amount DECIMAL(19,2) NOT NULL," +
                            "  currency VARCHAR(3) NOT NULL," +
                            "  date TIMESTAMP NOT NULL," +
                            "  note TEXT NULL," +
                            "  FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE," +
                            "  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL" +
                            ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS wallet_rule_strategies (" +
                            "  wallet_id VARCHAR(36) NOT NULL," +
                            "  strategy_class VARCHAR(255) NOT NULL," +
                            "  max_amount DECIMAL(19,2) NULL," +
                            "  PRIMARY KEY (wallet_id, strategy_class)," +
                            "  FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE" +
                            ")"
            );

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}

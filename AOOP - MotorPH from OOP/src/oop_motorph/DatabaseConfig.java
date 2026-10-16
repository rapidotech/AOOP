package oop_motorph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Opens an independent connection for each operation; callers own and close it. */
public final class DatabaseConfig {
    private static final DatabaseConfig INSTANCE = new DatabaseConfig();
    private DatabaseConfig() { }

    public static DatabaseConfig getInstance() { return INSTANCE; }

    public Connection getConnection() {
        Properties local = localConfiguration();
        String url = configured("url", "AOOP_DB_URL", local);
        String user = configured("user", "AOOP_DB_USER", local);
        String password = configured("password", "AOOP_DB_PASSWORD", local);
        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            throw new PersistenceException("Database is not configured. Set AOOP_DB_URL, AOOP_DB_USER and AOOP_DB_PASSWORD, or the aoop.db.url/user/password Java properties.");
        }
        // URL properties override JDBC Properties in Connector/J. Reject timeout overrides
        // so every application connection retains the same bounded network behavior.
        validateUrl(url);
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "15000");
        properties.setProperty("autoReconnect", "false");
        properties.setProperty("sslMode", "REQUIRED");
        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("connect to the database", e);
        }
    }

    private static void validateUrl(String url) {
        if (!url.startsWith("jdbc:mysql://")) { throw new PersistenceException("Database URL must use jdbc:mysql:// with TLS enabled."); }
        int queryStart = url.indexOf('?');
        if (queryStart < 0) { return; }
        try {
            for (String parameter : url.substring(queryStart + 1).split("&")) {
                String[] pair = parameter.split("=", 2);
                String key = java.net.URLDecoder.decode(pair[0], StandardCharsets.UTF_8).toLowerCase(java.util.Locale.ROOT);
                String value = pair.length > 1 ? java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                if (key.equals("connecttimeout") || key.equals("sockettimeout") || key.equals("autoreconnect")) {
                    throw new PersistenceException("Database URL must not override connectTimeout, socketTimeout or autoReconnect.");
                }
                if (key.equals("usessl") || key.equals("requiressl") || key.equals("verifyservercertificate")
                        || (key.equals("sslmode") && !value.equalsIgnoreCase("REQUIRED")
                        && !value.equalsIgnoreCase("VERIFY_CA") && !value.equalsIgnoreCase("VERIFY_IDENTITY"))) {
                    throw new PersistenceException("Database TLS cannot be disabled. Use sslMode=REQUIRED, VERIFY_CA or VERIFY_IDENTITY.");
                }
            }
        } catch (IllegalArgumentException failure) {
            throw new PersistenceException("Database URL contains an invalid option.");
        }
    }

    private static String configured(String suffix, String environmentName, Properties local) {
        String value = System.getProperty("aoop.db." + suffix);
        if (value != null) { return value; }
        value = System.getenv(environmentName);
        return value != null ? value : local.getProperty(suffix);
    }

    private static Properties localConfiguration() {
        Properties local = new Properties();
        // Fully supplied configuration does not depend on the fallback file.
        if (supplied("url", "AOOP_DB_URL") && supplied("user", "AOOP_DB_USER")
                && supplied("password", "AOOP_DB_PASSWORD")) { return local; }
        String configured = System.getProperty("aoop.db.config");
        if (configured == null) { configured = System.getenv("AOOP_DB_CONFIG"); }
        try {
            Path path = configured == null || configured.isBlank()
                    ? Paths.get(System.getProperty("user.home"), ".motorph", "database.properties")
                    : Paths.get(configured);
            if (!Files.exists(path)) { return local; }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                local.load(reader);
            }
            return local;
        } catch (IOException | IllegalArgumentException failure) {
            throw new PersistenceException("Unable to read local database configuration. Check the configured database properties file.");
        }
    }

    private static boolean supplied(String suffix, String environmentName) {
        return System.getProperty("aoop.db." + suffix) != null || System.getenv(environmentName) != null;
    }

    /** Compatibility no-op: connections are now closed by their individual callers. */
    @Deprecated
    public void closeConnection() { }
}

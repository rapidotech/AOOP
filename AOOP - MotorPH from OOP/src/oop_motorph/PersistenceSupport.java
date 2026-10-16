package oop_motorph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class PersistenceSupport {
    private PersistenceSupport() { }

    @FunctionalInterface
    interface Transaction { void run(Connection connection) throws SQLException; }

    static void inTransaction(String operation, Transaction transaction) {
        try (Connection connection = DatabaseConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                transaction.run(connection);
                connection.commit();
            } catch (SQLException | RuntimeException failure) {
                try { connection.rollback(); }
                catch (SQLException rollbackFailure) { failure.addSuppressed(rollbackFailure); }
                throw failure;
            }
        } catch (SQLException failure) {
            throw PersistenceException.databaseFailure(operation, failure);
        }
    }

    static PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
        return bounded(connection.prepareStatement(sql));
    }

    static PreparedStatement prepareStatement(Connection connection, String sql, int generatedKeys) throws SQLException {
        return bounded(connection.prepareStatement(sql, generatedKeys));
    }

    private static PreparedStatement bounded(PreparedStatement statement) throws SQLException {
        try {
            statement.setQueryTimeout(15);
            return statement;
        } catch (SQLException failure) {
            try { statement.close(); } catch (SQLException closeFailure) { failure.addSuppressed(closeFailure); }
            throw failure;
        }
    }

    static void requireOneRow(int rows) throws SQLException {
        if (rows != 1) { throw new SQLException("Expected exactly one matching record."); }
    }
}

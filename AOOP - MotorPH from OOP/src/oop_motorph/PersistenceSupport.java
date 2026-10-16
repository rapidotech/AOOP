package oop_motorph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class PersistenceSupport {
    private PersistenceSupport() { }

    @FunctionalInterface
    interface Transaction { void run(Connection connection) throws SQLException; }

    @FunctionalInterface
    interface TransactionResult<T> { T run(Connection connection) throws SQLException; }

    static void inTransaction(String operation, Transaction transaction) {
        inTransactionResult(operation, connection -> { transaction.run(connection); return null; });
    }

    static <T> T inTransactionResult(String operation, TransactionResult<T> transaction) {
        AuthorizationService.beginOperation();
        try (Connection connection = DatabaseConfig.getInstance().getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);
            try {
                T result = transaction.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException failure) {
                try { connection.rollback(); }
                catch (SQLException rollbackFailure) { failure.addSuppressed(rollbackFailure); }
                throw failure;
            }
        } catch (SQLException failure) {
            throw PersistenceException.databaseFailure(operation, failure);
        } finally {
            AuthorizationService.endOperation();
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

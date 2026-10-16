package oop_motorph;

/** A safe, user-displayable failure; never contains SQL text or connection credentials. */
public final class PersistenceException extends RuntimeException {
    public PersistenceException(String message) { super(message); }

    static PersistenceException databaseFailure(String operation, Exception failure) {
        // Driver messages and causes can contain connection URLs or personal data.
        return new PersistenceException("Unable to confirm the operation: " + operation + ". Check database configuration and availability before retrying.");
    }
}

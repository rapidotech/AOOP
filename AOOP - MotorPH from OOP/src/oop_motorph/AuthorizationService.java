package oop_motorph;

import java.sql.*;
import java.util.*;

/** Supported-client authorization. A shared JDBC credential cannot attest a modified client. */
public final class AuthorizationService {
    private static final Map<DatabaseHandler.AuthenticationResult, Identity> CAPABILITIES = new WeakHashMap<>();
    private static final ThreadLocal<Identity> AUTHORIZED_OPERATION = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IN_OPERATION = new ThreadLocal<>();
    private AuthorizationService() { }

    static final class Identity {
        final int userId;
        final String username, role, credentialFingerprint;
        Identity(int id, String username, String role, String hash) {
            userId = id; this.username = username; this.role = role;
            credentialFingerprint = AuditService.sha256(hash);
        }
    }

    static synchronized void issue(DatabaseHandler.AuthenticationResult result, int id, String username, String role, String hash) {
        if (result != null && !result.isResetRequired() && result.getEmployee() != null) {
            CAPABILITIES.put(result, new Identity(id, username, role, hash));
        }
    }

    static synchronized Identity consume(DatabaseHandler.AuthenticationResult result) {
        Identity identity = CAPABILITIES.remove(result);
        if (identity == null || result == null || result.isResetRequired()) {
            throw new AuthorizationException("Sign in with your current credentials before continuing.");
        }
        return identity;
    }

    static synchronized void revokeOutstanding() { CAPABILITIES.clear(); }

    public static Identity requireAuthenticated() {
        Identity identity = EmpUserSession.getInstance().identity();
        if (identity == null) { throw new AuthorizationException("Sign in before opening this page or performing this operation."); }
        return identity;
    }

    public static void requireHr() {
        if (!isHr(requireAuthenticated())) { throw new AuthorizationException("This operation requires an HR account."); }
    }

    public static void requireEmployeeOrHr(String employeeID) {
        Identity identity = requireAuthenticated();
        if (!isHr(identity) && !identity.username.equals(employeeID)) {
            throw new AuthorizationException("You may only access your own employee records.");
        }
    }

    static Identity requireAuthenticated(Connection connection) throws SQLException {
        Identity identity = requireAuthenticated();
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                "SELECT username,role,password,must_change_password FROM users WHERE UserID = ?")) {
            statement.setInt(1, identity.userId);
            try (ResultSet result = statement.executeQuery()) {
                boolean valid = result.next() && identity.username.equals(result.getString("username"))
                        && identity.role.equalsIgnoreCase(result.getString("role")) && !result.getBoolean("must_change_password")
                        && !result.wasNull() && identity.credentialFingerprint.equals(AuditService.sha256(Objects.toString(result.getString("password"), "")));
                if (!valid) {
                    EmpUserSession.getInstance().clearSession();
                    throw new AuthorizationException("Your account has changed or requires a password reset. Sign in again.");
                }
            }
        }
        if (Boolean.TRUE.equals(IN_OPERATION.get())) {
            Identity captured = AUTHORIZED_OPERATION.get();
            if (captured != null && captured != identity) { throw new AuthorizationException("The signed-in account changed during this operation. Retry after signing in."); }
            AUTHORIZED_OPERATION.set(identity);
        }
        return identity;
    }

    static void requireHr(Connection connection) throws SQLException {
        if (!isHr(requireAuthenticated(connection))) { throw new AuthorizationException("This operation requires an HR account."); }
    }

    static void requireEmployeeOrHr(Connection connection, String employeeID) throws SQLException {
        Identity identity = requireAuthenticated(connection);
        if (!isHr(identity) && !identity.username.equals(employeeID)) { throw new AuthorizationException("You may only access your own employee records."); }
    }

    static String employeeScope(Connection connection) throws SQLException {
        Identity identity = requireAuthenticated(connection);
        return isHr(identity) ? null : identity.username;
    }

    static boolean isHr(Identity identity) { return "HR".equalsIgnoreCase(identity.role); }
    static void beginOperation() { AUTHORIZED_OPERATION.remove(); IN_OPERATION.set(true); }
    static void endOperation() { AUTHORIZED_OPERATION.remove(); IN_OPERATION.remove(); }
    static Identity operationIdentity() {
        Identity identity = AUTHORIZED_OPERATION.get();
        if (identity == null) { throw new AuthorizationException("This write has no verified authorization context."); }
        return identity;
    }
}

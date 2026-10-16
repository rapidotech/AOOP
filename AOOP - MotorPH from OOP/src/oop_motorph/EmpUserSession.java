package oop_motorph;

/** A session can only be established from a committed, verified authentication result. */
public final class EmpUserSession {
    private static final EmpUserSession INSTANCE = new EmpUserSession();
    private EmpDetails currentUser;
    private AuthorizationService.Identity identity;
    private EmpUserSession() { }
    public static EmpUserSession getInstance() { return INSTANCE; }

    public synchronized void establish(DatabaseHandler.AuthenticationResult result) {
        AuthorizationService.Identity verified = AuthorizationService.consume(result);
        currentUser = result.getEmployee();
        identity = verified;
    }

    /** Caller-supplied users and role strings are not authentication proof. */
    @Deprecated
    public void setCurrentUser(EmpDetails user, String role) {
        throw new AuthorizationException("A user and role cannot create a session. Sign in with verified credentials.");
    }

    public synchronized EmpDetails getCurrentUser() { return identity == null ? null : currentUser; }
    public synchronized String getRole() { return identity == null ? null : identity.role; }
    synchronized AuthorizationService.Identity identity() { return identity; }
    public synchronized void clearSession() {
        currentUser = null;
        identity = null;
        AuthorizationService.revokeOutstanding();
    }
}

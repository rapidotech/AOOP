package oop_motorph;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Server-serialized, append-only audit calls; never logs passwords, hashes or row payloads. */
final class AuditService {
    private static final String GENESIS = "0".repeat(64);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'").withZone(ZoneOffset.UTC);
    private AuditService() { }

    static String sourceMachine() {
        String source = System.getenv("COMPUTERNAME");
        return bounded(source == null || source.isBlank() ? "unknown-machine" : source, 128);
    }

    static String sourceKey() { return sha256(sourceMachine()); }

    static void append(Connection connection, String actor, String action, String target, String outcome, String details) throws SQLException {
        if (connection.getAutoCommit()) { throw new SQLException("Audit requires an explicit transaction."); }
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection, "CALL motorph_append_audit(?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, bounded(actor, 128)); statement.setString(2, bounded(action, 64));
            statement.setString(3, bounded(target, 128)); statement.setString(4, bounded(outcome, 32));
            statement.setString(5, sourceMachine()); statement.setString(6, bounded(details, 512));
            statement.execute();
        }
    }

    static void business(Connection connection, String action, String target) throws SQLException {
        append(connection, AuthorizationService.operationIdentity().username, action, target, "SUCCESS", "supported_client_operation");
    }

    static void businessTransition(Connection connection, String action, String target, String previous, String next) throws SQLException {
        Set<String> statuses=Set.of("Pending","Verified","Approved","Rejected");
        if (!Set.of("ATTENDANCE_STATUS_CHANGED","PAYROLL_STATUS_CHANGED").contains(action)
                || !statuses.contains(previous) || !statuses.contains(next)) {
            throw new IllegalArgumentException("This transition metadata is not supported.");
        }
        append(connection, AuthorizationService.operationIdentity().username, action, target, "SUCCESS",
                "status_before="+previous+";status_after="+next);
    }

    static List<AuditEvent> auditEvents(Connection connection, long beforeEventId) throws SQLException {
        List<AuditEvent> events=new ArrayList<>();
        String sql="SELECT EventID,OccurredAtUTC,ActorID,Action,TargetID,Outcome,SourceMachine,Details FROM AuditLog"
                +(beforeEventId==0?"":" WHERE EventID < ?")+" ORDER BY EventID DESC LIMIT 100";
        try (PreparedStatement statement=PersistenceSupport.prepareStatement(connection,sql)) {
            if (beforeEventId>0) { statement.setLong(1,beforeEventId); }
            try (ResultSet result=statement.executeQuery()) {
                while(result.next()) {
                    events.add(new AuditEvent(result.getLong("EventID"),instant(result,"OccurredAtUTC"),
                            result.getString("ActorID"),result.getString("Action"),result.getString("TargetID"),
                            result.getString("Outcome"),result.getString("SourceMachine"),result.getString("Details")));
                }
            }
        }
        return List.copyOf(events);
    }

    static List<SecurityAlert> securityAlerts(Connection connection, long afterEventId) throws SQLException {
        List<SecurityAlert> alerts = new ArrayList<>();
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                "SELECT EventID,OccurredAtUTC,TargetID,SourceMachine,Action FROM AuditLog WHERE EventID > ? AND Action IN ('AUTH_FAILURE','AUTH_LOCKED') ORDER BY EventID DESC LIMIT 100")) {
            statement.setLong(1, Math.max(0, afterEventId));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) { alerts.add(new SecurityAlert(result.getLong(1), instant(result, "OccurredAtUTC"), result.getString(3), result.getString(4), result.getString(5))); }
            }
        }
        return alerts;
    }

    static boolean verify(Connection connection) throws SQLException {
        long head;
        String expectedHead;
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection, "SELECT LastEventID,LastHash FROM AuditChainHead WHERE ChainID=1"); ResultSet result = statement.executeQuery()) {
            if (!result.next()) { return false; }
            head = result.getLong(1); expectedHead = result.getString(2);
        }
        String previous = GENESIS;
        long expected = 1;
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                "SELECT EventID,OccurredAtUTC,ActorID,Action,TargetID,Outcome,SourceMachine,Details,PreviousHash,EntryHash FROM AuditLog ORDER BY EventID")) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    long id = result.getLong("EventID");
                    String storedPrevious = result.getString("PreviousHash");
                    String storedHash = result.getString("EntryHash");
                    if (id != expected++ || !previous.equals(storedPrevious)) { return false; }
                    String computed = eventHash(previous, id, instant(result, "OccurredAtUTC"), result.getString("ActorID"),
                            result.getString("Action"), result.getString("TargetID"), result.getString("Outcome"), result.getString("SourceMachine"), result.getString("Details"));
                    if (!computed.equals(storedHash)) { return false; }
                    previous = storedHash;
                }
            }
        }
        return expected - 1 == head && previous.equals(expectedHead);
    }

    static String eventHash(String previous, long id, Instant time, String... fields) {
        StringBuilder canonical = new StringBuilder(previous).append('|').append(id).append('|').append(TIME.format(time));
        for (String field : fields) { canonical.append('|').append(HexFormat.of().withUpperCase().formatHex(field.getBytes(StandardCharsets.UTF_8))); }
        return sha256(canonical.toString());
    }

    static Instant instant(ResultSet result, String column) throws SQLException {
        Timestamp timestamp = result.getTimestamp(column, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        return timestamp == null ? null : timestamp.toInstant();
    }

    static String sha256(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException failure) { throw new IllegalStateException("SHA-256 is unavailable."); }
    }

    private static String bounded(String value, int limit) {
        if (value == null) { return ""; }
        return value.replaceAll("[\\p{Cntrl}]", "_").substring(0, Math.min(limit, value.length()));
    }
}

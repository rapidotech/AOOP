package oop_motorph;

import java.time.Instant;

public final class SecurityAlert {
    private final long eventId;
    private final Instant occurredAt;
    private final String username, sourceMachine, action;
    SecurityAlert(long id, Instant time, String username, String source, String action) {
        eventId = id; occurredAt = time; this.username = username; sourceMachine = source; this.action = action;
    }
    public long getEventId() { return eventId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getUsername() { return username; }
    public String getSourceMachine() { return sourceMachine; }
    public String getAction() { return action; }
}

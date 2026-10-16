package oop_motorph;

import java.time.Instant;

/** Immutable audit-view metadata; excludes password hashes, chain hashes and business row payloads. */
public final class AuditEvent {
    private final long eventId;
    private final Instant occurredAt;
    private final String actor, action, target, outcome, sourceMachine, details;

    AuditEvent(long eventId, Instant occurredAt, String actor, String action, String target,
            String outcome, String sourceMachine, String details) {
        this.eventId=eventId; this.occurredAt=occurredAt; this.actor=actor; this.action=action;
        this.target=target; this.outcome=outcome; this.sourceMachine=sourceMachine; this.details=details;
    }
    public long getEventId() { return eventId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getTarget() { return target; }
    public String getOutcome() { return outcome; }
    public String getSourceMachine() { return sourceMachine; }
    public String getDetails() { return details; }
}

package oop_motorph;

/**
 * Retired CSV-era migration entry points retained for source compatibility.
 * The normalized schema uses protected administrator migration tools and
 * authorized, transactionally audited application APIs.
 */
@Deprecated
public class DataMigration {
    /** The obsolete importer cannot safely operate on the normalized schema. */
    @Deprecated
    public static void main(String[] args) {
        AuthorizationService.requireHr();
        throw new PersistenceException("Legacy CSV database migration is retired. Use the protected administrator migration tools and the validated importLegacyLedgers API.");
    }

    /** Payroll generation now belongs to the supported HR processing workflow. */
    @Deprecated
    public static void generatePayrollFromAttendance() {
        AuthorizationService.requireHr();
        throw new PersistenceException("The legacy payroll generator is retired. Use the HR payroll processing screen and authorized, audited database ledger APIs.");
    }
}

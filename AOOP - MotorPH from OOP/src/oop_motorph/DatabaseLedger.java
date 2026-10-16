package oop_motorph;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;

/** Legacy row-shaped APIs backed by scoped, audited database transactions. */
final class DatabaseLedger {
    private static final String ATTENDANCE = "AttendanceRequests", PAYROLL = "PayrollLedger";
    private DatabaseLedger() { }

    static List<String[]> attendanceRows() { return read(ATTENDANCE, 20); }
    static List<String[]> payrollRows() { return read(PAYROLL, 22); }

    private static List<String[]> read(String table, int columns) {
        AuthorizationService.requireAuthenticated();
        return PersistenceSupport.inTransactionResult("read database ledger", connection -> {
            String scope = AuthorizationService.employeeScope(connection);
            List<Row> records = select(connection, table, columns, scope, null, null, null, false);
            List<String[]> rows = new ArrayList<>();
            for (Row row : records) { rows.add(row.values); }
            return rows;
        });
    }

    static void addAttendance(String[] input) {
        String[] captured = input == null ? null : input.clone();
        validate(captured, 20);
        AuthorizationService.requireEmployeeOrHr(captured[0]);
        PersistenceSupport.inTransaction("save attendance request", connection -> {
            AuthorizationService.requireEmployeeOrHr(connection, captured[0]);
            String[] row = canonicalAttendance(connection, captured);
            if (!"Pending".equals(row[13])) {
                throw new IllegalArgumentException("New attendance requests must begin with Pending status.");
            }
            if (!select(connection, ATTENDANCE, 20, row[0], date(row[6]), date(row[7]), null, true).isEmpty()) {
                throw new PersistenceException("An attendance request already exists for this employee and date range. Edit that request instead.");
            }
            insert(connection, ATTENDANCE, row);
            AuditService.business(connection, "ATTENDANCE_REQUEST_CREATED", key(row[0], row[6], row[7]));
        });
    }

    static void editAttendance(String[] input, String oldFrom, String oldTo) {
        String[] captured = input == null ? null : input.clone();
        validate(captured, 20);
        AuthorizationService.requireEmployeeOrHr(captured[0]);
        LocalDate previousFrom = date(oldFrom), previousTo = date(oldTo);
        PersistenceSupport.inTransaction("edit attendance request", connection -> {
            AuthorizationService.requireEmployeeOrHr(connection, captured[0]);
            Row existing = unique(select(connection, ATTENDANCE, 20, captured[0], previousFrom, previousTo, null, true), "attendance request");
            String[] row = canonicalAttendance(connection, captured);
            if (!Objects.equals(existing.values[13], row[13])) {
                throw new IllegalArgumentException("Editing a request cannot change its approval status. Use the approval action.");
            }
            if (!AuthorizationService.isHr(AuthorizationService.operationIdentity())
                    && (!"Pending".equals(existing.values[13]) || !"Pending".equals(row[13]))) {
                throw new AuthorizationException("You may only edit your own Pending attendance requests.");
            }
            update(connection, ATTENDANCE, existing.id, row);
            AuditService.business(connection, "ATTENDANCE_REQUEST_UPDATED", key(row[0], row[6], row[7]));
        });
    }

    static void attendanceStatus(String employee, String from, String to, String status) {
        AuthorizationService.requireHr(); validateStatus(status);
        PersistenceSupport.inTransaction("approve attendance request", connection -> {
            AuthorizationService.requireHr(connection);
            Row existing = unique(select(connection, ATTENDANCE, 20, employee, optionalDate(from), optionalDate(to), null, true), "attendance request");
            String previous=existing.values[13];
            requireTransition(existing.values[13], status, true);
            existing.values[13] = status;
            update(connection, ATTENDANCE, existing.id, existing.values);
            AuditService.businessTransition(connection, "ATTENDANCE_STATUS_CHANGED", key(employee, existing.values[6], existing.values[7]), previous, status);
        });
    }

    static void savePayroll(String[] input) {
        String[] captured = input == null ? null : input.clone();
        AuthorizationService.requireHr(); validate(captured, 22);
        PersistenceSupport.inTransaction("save payroll ledger", connection -> {
            AuthorizationService.requireHr(connection);
            String[] row = captured.clone();
            List<Row> existing = select(connection, PAYROLL, 22, row[1], date(row[4]), date(row[5]), null, true);
            if (existing.isEmpty()) {
                requirePending(row[21]); insert(connection, PAYROLL, row);
            } else {
                Row matched = unique(existing, "payroll period"); row[0] = matched.values[0];
                requireEditablePayroll(matched.values, row);
                update(connection, PAYROLL, matched.id, row);
            }
            AuditService.business(connection, "PAYROLL_LEDGER_SAVED", key(row[1], row[4], row[5]));
        });
    }

    static void replacePayroll(List<String[]> rows) {
        AuthorizationService.requireHr();
        Objects.requireNonNull(rows, "Payroll records are required.");
        for (String[] row : rows) { validate(row, 22); }
        PersistenceSupport.inTransaction("replace payroll ledger", connection -> {
            AuthorizationService.requireHr(connection);
            List<Row> existing = select(connection, PAYROLL, 22, null, null, null, null, true);
            Set<Long> retained = new HashSet<>();
            for (String[] row : rows) {
                List<Row> matches = select(connection, PAYROLL, 22, row[1], date(row[4]), date(row[5]), row[0], true);
                if (matches.isEmpty()) { requirePending(row[21]); insert(connection, PAYROLL, row); }
                else {
                    Row match = unique(matches, "payroll transaction and period");
                    requireEditablePayroll(match.values, row);
                    update(connection, PAYROLL, match.id, row); retained.add(match.id);
                }
            }
            if (existing.stream().anyMatch(row -> !retained.contains(row.id))) {
                throw new IllegalArgumentException("A bulk save cannot erase existing payroll periods. Retain every existing transaction.");
            }
            AuditService.business(connection, "PAYROLL_LEDGER_SAVED", "payroll_ledger");
        });
    }

    static void payrollStatus(String transaction, String employee, String from, String to, String status) {
        AuthorizationService.requireHr(); validateStatus(status);
        PersistenceSupport.inTransaction("approve payroll ledger", connection -> {
            AuthorizationService.requireHr(connection);
            Row existing = unique(select(connection, PAYROLL, 22, employee, optionalDate(from), optionalDate(to), transaction, true), "payroll transaction and period");
            String previous=existing.values[21];
            requireTransition(existing.values[21], status, false);
            existing.values[21] = status;
            update(connection, PAYROLL, existing.id, existing.values);
            AuditService.businessTransition(connection, "PAYROLL_STATUS_CHANGED", existing.values[0], previous, status);
        });
    }

    static void importLegacy(List<String[]> attendance, List<String[]> payroll) {
        AuthorizationService.requireHr();
        Objects.requireNonNull(attendance, "Attendance records are required."); Objects.requireNonNull(payroll, "Payroll records are required.");
        for (String[] row : attendance) { validate(row, 20); }
        for (String[] row : payroll) { validate(row, 22); }
        PersistenceSupport.inTransaction("import legacy ledgers", connection -> {
            AuthorizationService.requireHr(connection);
            importRows(connection, ATTENDANCE, attendance); importRows(connection, PAYROLL, payroll);
            AuditService.business(connection, "LEGACY_LEDGERS_IMPORTED", "explicit_local_import");
        });
    }

    private static void importRows(Connection connection, String table, List<String[]> records) throws SQLException {
        boolean attendance = ATTENDANCE.equals(table);
        for (String[] row : records) {
            String employee = row[attendance ? 0 : 1];
            List<Row> existing = select(connection, table, row.length, employee, date(row[attendance ? 6 : 4]), date(row[attendance ? 7 : 5]), null, true);
            if (existing.isEmpty()) { insert(connection, table, row); }
            else if (!Arrays.equals(unique(existing, "legacy record").values, row)) {
                throw new PersistenceException("A legacy record conflicts with an existing database ledger record. Resolve the conflict before importing; nothing was overwritten.");
            }
        }
    }

    private static String[] canonicalAttendance(Connection connection, String[] input) throws SQLException {
        List<EmpDetails> employees = DatabaseHandler.getEmployeeData(connection, input[0]);
        if (employees.size() != 1) { throw new PersistenceException("The employee profile is missing. No attendance request was saved."); }
        EmpDetails employee = employees.get(0);
        String[] row = input.clone();
        row[0]=employee.getEmpID(); row[1]=employee.getFirstName(); row[2]=employee.getLastName();
        row[3]=employee.getEmployeeStatus(); row[4]=employee.getPosition(); row[5]=employee.getImmediateSupervisor();
        return row;
    }

    private static List<Row> select(Connection connection, String table, int columns, String employee, LocalDate from, LocalDate to, String transaction, boolean lock) throws SQLException {
        String idColumn = ATTENDANCE.equals(table) ? "RequestID" : "LedgerID";
        StringBuilder sql = new StringBuilder("SELECT ").append(idColumn).append(" AS RecordID,Status");
        for (int i=0;i<columns;i++) { sql.append(",JSON_UNQUOTE(JSON_EXTRACT(RecordData,'$[").append(i).append("]')) AS C").append(i); }
        sql.append(" FROM ").append(table).append(" WHERE 1=1");
        if (employee!=null) { sql.append(" AND EmployeeID=?"); }
        if (from!=null) { sql.append(" AND DateFrom=?"); }
        if (to!=null) { sql.append(" AND DateTo=?"); }
        if (transaction!=null && PAYROLL.equals(table)) { sql.append(" AND TransactionID=?"); }
        sql.append(" ORDER BY ").append(idColumn);
        if (lock) { sql.append(" FOR UPDATE"); }
        List<Row> rows = new ArrayList<>();
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection, sql.toString())) {
            int parameter=1;
            if (employee!=null) { statement.setString(parameter++,employee); }
            if (from!=null) { statement.setDate(parameter++,java.sql.Date.valueOf(from)); }
            if (to!=null) { statement.setDate(parameter++,java.sql.Date.valueOf(to)); }
            if (transaction!=null && PAYROLL.equals(table)) { statement.setString(parameter,transaction); }
            try (ResultSet result=statement.executeQuery()) {
                while(result.next()) {
                    String[] row=new String[columns];
                    for(int i=0;i<columns;i++) { row[i]=result.getString("C"+i); }
                    row[ATTENDANCE.equals(table)?13:21]=result.getString("Status");
                    validate(row,columns);
                    rows.add(new Row(result.getLong("RecordID"),row));
                }
            }
        }
        return rows;
    }

    private static void insert(Connection connection,String table,String[] row) throws SQLException {
        boolean attendance=ATTENDANCE.equals(table);
        String sql="INSERT INTO "+table+" ("+(attendance?"":"TransactionID,")+"EmployeeID,DateFrom,DateTo,Status,RecordData) VALUES ("+(attendance?"":"?,")+"?,?,?,?,JSON_ARRAY("+String.join(",",Collections.nCopies(row.length,"?"))+"))";
        try(PreparedStatement statement=PersistenceSupport.prepareStatement(connection,sql)) {
            bind(statement,row,attendance,false,0);
            PersistenceSupport.requireOneRow(statement.executeUpdate());
        }
    }

    private static void update(Connection connection,String table,long id,String[] row) throws SQLException {
        boolean attendance=ATTENDANCE.equals(table);
        String sql="UPDATE "+table+" SET "+(attendance?"":"TransactionID=?,")+"EmployeeID=?,DateFrom=?,DateTo=?,Status=?,RecordData=JSON_ARRAY("+String.join(",",Collections.nCopies(row.length,"?"))+") WHERE "+(attendance?"RequestID":"LedgerID")+"=?";
        try(PreparedStatement statement=PersistenceSupport.prepareStatement(connection,sql)) {
            bind(statement,row,attendance,true,id);
            PersistenceSupport.requireOneRow(statement.executeUpdate());
        }
    }

    private static void bind(PreparedStatement statement,String[] row,boolean attendance,boolean update,long id) throws SQLException {
        int parameter=1;
        if(!attendance) { statement.setString(parameter++,row[0]); }
        statement.setString(parameter++,row[attendance?0:1]);
        statement.setDate(parameter++,java.sql.Date.valueOf(date(row[attendance?6:4])));
        statement.setDate(parameter++,java.sql.Date.valueOf(date(row[attendance?7:5])));
        statement.setString(parameter++,row[attendance?13:21]);
        for(String value:row) { statement.setString(parameter++,value); }
        if(update) { statement.setLong(parameter,id); }
    }

    private static Row unique(List<Row> rows,String kind) {
        if(rows.isEmpty()) { throw new PersistenceException("No matching database "+kind+" was found. No changes were saved."); }
        if(rows.size()!=1) { throw new PersistenceException("Multiple records match. Select the exact employee, transaction and date range."); }
        return rows.get(0);
    }

    static LocalDate date(String value) {
        if(value==null || value.isBlank()) { throw new IllegalArgumentException("Record dates are required."); }
        for(String pattern:List.of("uuuu-MM-dd","dd-MMM-uu","M/d/uuuu","dd/MM/uuuu")) {
            try { return LocalDate.parse(value,DateTimeFormatter.ofPattern(pattern,Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT)); }
            catch(DateTimeParseException ignored) { }
        }
        throw new IllegalArgumentException("Use a valid record date.");
    }
    private static LocalDate optionalDate(String value) { return value==null?null:date(value); }
    private static String key(String employee,String from,String to) { return employee+":"+date(from)+":"+date(to); }
    private static void validateStatus(String value) {
        if(value==null || value.isBlank() || value.length()>32 || value.chars().anyMatch(Character::isISOControl)) { throw new IllegalArgumentException("Choose a valid record status."); }
    }
    private static void requirePending(String status) {
        if (!"Pending".equals(status)) { throw new IllegalArgumentException("New payroll periods must begin with Pending status."); }
    }
    private static void requireEditablePayroll(String[] existing, String[] replacement) {
        if (!Objects.equals(existing[21], replacement[21])) {
            throw new IllegalArgumentException("Saving payroll cannot change its approval status. Use the verification or approval action.");
        }
        if (!"Pending".equals(existing[21]) && !Arrays.equals(existing, replacement)) {
            throw new IllegalArgumentException("Undo verification or approval before editing payroll amounts.");
        }
    }
    private static void requireTransition(String previous, String next, boolean attendance) {
        boolean allowed = attendance ? "Pending".equals(previous) && Set.of("Approved", "Rejected").contains(next)
                : "Pending".equals(previous) && "Verified".equals(next)
                || "Verified".equals(previous) && Set.of("Approved", "Pending").contains(next)
                || "Approved".equals(previous) && "Pending".equals(next);
        if (!allowed) { throw new IllegalArgumentException("This status transition is unavailable. Reload the record and use its next permitted action."); }
    }
    private static void validate(String[] row,int columns) {
        if(row==null || row.length!=columns) { throw new IllegalArgumentException("The ledger record has an unexpected column count."); }
        for(String value:row) { if(value==null || value.length()>4096) { throw new IllegalArgumentException("Ledger fields must be present and at most4096 characters."); } }
        int employee=columns==20?0:1,from=columns==20?6:4,to=columns==20?7:5,status=columns==20?13:21;
        if(!row[employee].matches("[0-9]{1,10}")) { throw new IllegalArgumentException("Use a valid employee ID."); }
        if(date(row[to]).isBefore(date(row[from]))) { throw new IllegalArgumentException("The record end date cannot precede its start date."); }
        validateStatus(row[status]);
        if (columns==20) {
            for (int index : new int[]{10,11,14,15,16,17,18,19}) {
                try {
                    java.math.BigDecimal value = new java.math.BigDecimal(row[index]);
                    if (value.signum()<0 || !Double.isFinite(value.doubleValue())) { throw new NumberFormatException(); }
                } catch (NumberFormatException failure) {
                    throw new IllegalArgumentException("Attendance hours, duration and leave balances must be finite nonnegative numbers.");
                }
            }
        }
        if (columns==22) {
            for (int index=6; index<=20; index++) {
                try {
                    java.math.BigDecimal value = new java.math.BigDecimal(row[index]);
                    if (!Double.isFinite(value.doubleValue())) { throw new NumberFormatException(); }
                } catch (NumberFormatException failure) {
                    throw new IllegalArgumentException("Payroll amounts, hours and adjustments must be finite numbers.");
                }
            }
        }
        if(columns==22 && (row[0].isBlank() || row[0].length()>64)) { throw new IllegalArgumentException("Use a valid payroll transaction ID."); }
    }
    private static final class Row { final long id;final String[] values;Row(long id,String[] values){this.id=id;this.values=values;} }
}

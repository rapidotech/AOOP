package oop_motorph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

/** Writable legacy records, kept separate from normalized database time logs. */
final class LocalRecordStore {
    static final String ATTENDANCE = "CSV_Attendance.csv";
    static final String PAYROLL = "CSV_Payroll.csv";
    static final String[] ATTENDANCE_HEADER = {"EmpID", "First Name", "Last Name", "Status", "Position", "ImmediateSup", "DateFrom", "DateTo", "TimeIn", "TimeOut", "HoursWorked", "Duration", "AttendanceType", "AttendanceStat", "VLCount", "VLUsed", "VLBal", "SLCount", "SLUsed", "SLBal"};
    static final String[] PAYROLL_HEADER = {"Transac ID", "EmpID", "FirstName", "LastName", "Payroll Date From", "Payroll Date To", "Basic", "Hourly Rate", "Allowances", "Hrs Per Month", "Hrs Worked", "Adj Earnings", "Total Earnings", "Tardiness Hours", "EE Tax Deduction", "EE SSS Deduction", "EE Pagibig Deduction", "EE Philhealth Deduction", "Adj Deductions", "Total Deduction", "Netpay", "PayStatus"};
    private LocalRecordStore() { }

    private static Path directory() {
        String configured = System.getProperty("aoop.data.dir");
        if (configured == null || configured.isBlank()) { configured = System.getenv("AOOP_DATA_DIR"); }
        return configured == null || configured.isBlank()
                ? Paths.get(System.getProperty("user.home"), ".motorph") : Paths.get(configured);
    }

    static synchronized List<String[]> read(String name) {
        try {
            Path file = directory().resolve(name);
            if (Files.notExists(file)) { return new ArrayList<>(); }
            try (Reader input = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                List<String[]> rows = parse(input);
                if (rows.isEmpty() || !Arrays.equals(rows.remove(0), header(name))) {
                    throw new IOException("Unexpected CSV header.");
                }
                validate(name, rows);
                return rows;
            }
        } catch (IOException | InvalidPathException failure) {
            throw new PersistenceException("Unable to read local attendance/payroll records. Check the configured data directory.");
        }
    }

    static synchronized void update(String name, Consumer<List<String[]>> update) {
        List<String[]> rows = read(name);
        update.accept(rows);
        write(name, rows);
    }

    static synchronized void write(String name, List<String[]> rows) {
        validate(name, rows);
        Path temporary = null;
        try {
            Path directory = directory();
            Files.createDirectories(directory);
            Path target = directory.resolve(name);
            temporary = Files.createTempFile(directory, name, ".tmp");
            try (BufferedWriter output = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writeRow(output, header(name));
                for (String[] row : rows) { writeRow(output, row); }
            }
            // A failed replacement leaves the previous complete record file intact.
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            temporary = null;
        } catch (IOException | InvalidPathException failure) {
            throw new PersistenceException("Unable to save local attendance/payroll records. The previous records were preserved; check the data directory and retry.");
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }

    private static String[] header(String name) {
        if (ATTENDANCE.equals(name)) { return ATTENDANCE_HEADER; }
        if (PAYROLL.equals(name)) { return PAYROLL_HEADER; }
        throw new IllegalArgumentException("Unknown local record type.");
    }

    private static void validate(String name, List<String[]> rows) {
        int columns = header(name).length;
        if (rows == null) { throw new IllegalArgumentException("Records are required."); }
        for (String[] row : rows) {
            if (row == null || row.length != columns) {
                throw new PersistenceException("Local attendance/payroll records have an unexpected column count. No records were saved.");
            }
        }
    }

    private static void writeRow(Writer output, String[] row) throws IOException {
        for (int i = 0; i < row.length; i++) {
            if (i > 0) { output.write(','); }
            String value = row[i] == null ? "" : row[i];
            output.write('"');
            output.write(value.replace("\"", "\"\""));
            output.write('"');
        }
        output.write('\n');
    }

    private static List<String[]> parse(Reader source) throws IOException {
        PushbackReader reader = new PushbackReader(source, 1);
        List<String[]> rows = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        int next;
        while ((next = reader.read()) != -1) {
            char value = (char) next;
            if (value == '"') {
                if (quoted) {
                    int after = reader.read();
                    if (after == '"') { field.append('"'); }
                    else { quoted = false; if (after != -1) { reader.unread(after); } }
                } else if (field.length() == 0) { quoted = true; }
                else { throw new IOException("Malformed CSV quote."); }
            } else if (!quoted && value == ',') {
                fields.add(field.toString()); field.setLength(0);
            } else if (!quoted && (value == '\n' || value == '\r')) {
                if (value == '\r') {
                    int after = reader.read();
                    if (after != '\n' && after != -1) { reader.unread(after); }
                }
                fields.add(field.toString()); field.setLength(0);
                rows.add(fields.toArray(new String[0])); fields.clear();
            } else { field.append(value); }
        }
        if (quoted) { throw new IOException("Unclosed CSV quote."); }
        if (field.length() > 0 || !fields.isEmpty()) {
            fields.add(field.toString()); rows.add(fields.toArray(new String[0]));
        }
        return rows;
    }
}

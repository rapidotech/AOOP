package oop_motorph;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableModel;

public class CSVHandler {

    // File paths
    public static final String CSV_AttendancePath = "oop_motorph/resources/CSV_Attendance.csv";
    public static final String CSV_EmployeesPath = "oop_motorph/resources/CSV_Employees.csv";
    public static final String CSV_PayrollPath = "oop_motorph/resources/CSV_Payroll.csv";
    public static final String CSV_UsersPath = "oop_motorph/resources/CSV_Users.csv";
    public static final String CSV_SalaryPath = "oop_motorph/resources/CSV_Salary.csv";

    // private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm:ss");

    // User Credentials
    public static Map<String, String[]> loadCredentials() {
        return DatabaseHandler.loadCredentials();
    }

    public static boolean employeeExists(String empID, String existingEmpID) {
        return DatabaseHandler.employeeExists(empID, existingEmpID);
    }

    // Read employee data and return a list of EmpDetails objects
    public static List<EmpDetails> getEmployeeData() {
        return DatabaseHandler.getEmployeeData();
    }

    // Update employee record
    public static void updateEmployee(EmpDetails updatedEmp) {
        DatabaseHandler.updateEmployee(updatedEmp);
    }

    private static void updateAttendanceOrSalary(EmpDetails updatedEmp) {      //-----------???????
        DatabaseHandler.updateEmployee(updatedEmp);
    }

    public static void addEmployee(EmpDetails newEmp) {
        DatabaseHandler.addEmployee(newEmp);
    }

    public static void deleteEmployee(String empID) {
        DatabaseHandler.deleteEmployee(empID);
    }

    // Reads attendance data and returns a list of EmpAttLeave objects
    public static List<EmpAttLeave> getAttendanceData() {
        List<EmpAttLeave> attendance = new ArrayList<>(DatabaseHandler.getAttendanceData());
        attendance.addAll(loadAttendanceRequests());
        return attendance;
    }

    // Reads salary data and returns a list of EmpSalaryDetails objects
    public static List<EmpSalaryDetails> getSalaryData() {
        return DatabaseHandler.getSalaryData();
    }

    public static void addEmployeeSalary(EmpSalaryDetails newEmpSalary) {
        DatabaseHandler.addEmployeeSalary(newEmpSalary);
    }

    public static void updateEmployeeSalary(EmpSalaryDetails updatedSalary) {
        // Delegate to DatabaseHandler instead of using CSV
        DatabaseHandler.updateEmployeeSalary(updatedSalary);
    }

    // Add or Update employee details in the database instead of CSV files
    public static void addOrUpdateEmployeeInSalary(EmpDetails updatedEmp) {
        // Check if employee exists
        if (DatabaseHandler.employeeExists(updatedEmp.getEmpID(), null)) {
            // Update existing employee
            DatabaseHandler.updateEmployee(updatedEmp);
        } else {
            // Add new employee (this will also create compensation and government ID records)
            DatabaseHandler.addEmployee(updatedEmp);
        }
    }

    public static void saveAttendanceRequest(EmpAttLeave empAttLeave) {
        Objects.requireNonNull(empAttLeave, "Attendance request is required.");
        String[] row = attendanceRow(empAttLeave);
        LocalRecordStore.update(LocalRecordStore.ATTENDANCE, rows -> {
            for (String[] existing : rows) {
                if (Objects.equals(existing[0], row[0]) && sameDate(existing[6], row[6]) && sameDate(existing[7], row[7])) {
                    throw new PersistenceException("An attendance request already exists for this employee and date range. Edit that request instead.");
                }
            }
            rows.add(row);
        });
    }

    public static void saveEditAttendanceRequest(EmpAttLeave empAttLeave, String oldDateFrom, String oldDateTo) {
        Objects.requireNonNull(empAttLeave, "Attendance request is required.");
        String[] replacement = attendanceRow(empAttLeave);
        LocalRecordStore.update(LocalRecordStore.ATTENDANCE, rows -> {
            int selected = attendanceIndex(rows, empAttLeave.getEmpID(), oldDateFrom, oldDateTo);
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (i != selected && Objects.equals(row[0], replacement[0])
                        && sameDate(row[6], replacement[6]) && sameDate(row[7], replacement[7])) {
                    throw new PersistenceException("Another attendance request already uses that date range. No changes were saved.");
                }
            }
            rows.set(selected, replacement);
        });
    }

    /** Local requests are explicit: normalized database time logs keep their existing API. */
    public static List<EmpAttLeave> loadAttendanceRequests() {
        List<EmpAttLeave> requests = new ArrayList<>();
        for (String[] row : LocalRecordStore.read(LocalRecordStore.ATTENDANCE)) {
            requests.add(new EmpAttLeave(row[0], row[1], row[2], row[3], row[4], row[5],
                    parseDate(row[6]), parseDate(row[7]), row[8].isEmpty() ? null : parseTime(row[8]),
                    row[9].isEmpty() ? null : parseTime(row[9]), parseDouble(row[10]), parseDouble(row[11]),
                    row[12], row[13], parseDouble(row[14]), parseDouble(row[15]), parseDouble(row[16]),
                    parseDouble(row[17]), parseDouble(row[18]), parseDouble(row[19])));
        }
        return requests;
    }

    public static List<String[]> loadPayrollLedger() {
        return LocalRecordStore.read(LocalRecordStore.PAYROLL);
    }

    private static String[] attendanceRow(EmpAttLeave request) {
        if (request.getAttDateFrom() == null || request.getAttDateTo() == null) {
            throw new IllegalArgumentException("Attendance dates are required.");
        }
        return new String[]{request.getEmpID(), request.getFirstName(), request.getLastName(),
                request.getEmployeeStatus(), request.getPosition(), request.getImmediateSupervisor(),
                request.getAttDateFrom().format(DATE_FORMATTER), request.getAttDateTo().format(DATE_FORMATTER),
                request.getTimeIn() == null ? "" : request.getTimeIn().format(TIME_FORMATTER),
                request.getTimeOut() == null ? "" : request.getTimeOut().format(TIME_FORMATTER),
                String.valueOf(request.getHoursWorked()), String.valueOf(request.getDuration()),
                request.getAttendanceType(), request.getAttendanceStatus(), String.valueOf(request.getVlCount()),
                String.valueOf(request.getVlUsed()), String.valueOf(request.getVlBalance()),
                String.valueOf(request.getSlCount()), String.valueOf(request.getSlUsed()), String.valueOf(request.getSlBalance())};
    }

    // Attendance Approval
    public static void updateAttendanceStatus(String empID, String newStatus, String message) {
        updateAttendanceStatus(empID, null, null, newStatus, message);
    }

    public static void updateAttendanceStatus(String empID, String dateFrom, String dateTo, String newStatus, String message) {
        LocalRecordStore.update(LocalRecordStore.ATTENDANCE, rows -> {
            rows.get(attendanceIndex(rows, empID, dateFrom, dateTo))[13] = newStatus;
        });
    }

    private static int attendanceIndex(List<String[]> rows, String empID, String dateFrom, String dateTo) {
        int found = -1;
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (Objects.equals(row[0], empID) && (dateFrom == null || sameDate(row[6], dateFrom))
                    && (dateTo == null || sameDate(row[7], dateTo))) {
                if (found >= 0) { throw new PersistenceException("Multiple local attendance requests match. Select a specific employee and date range."); }
                found = i;
            }
        }
        if (found < 0) { throw new PersistenceException("No local attendance request matches this employee and date range. Database time logs do not contain approval status."); }
        return found;
    }

    private static boolean sameDate(String first, String second) {
        return parseDate(first).equals(parseDate(second));
    }

    public static void savePayrollData(

            String empID, String firstName, String lastName,
            String payDateFrom, String payDateTo, String basicSalary, String hourlyRate,
            String totalAllowances, String hrsPerMonth, String totalHrsWorked, String adjEarnings,
            String totalEarnings, String tardinessAbsences, String eeTax,
            String eeSSS, String eePagibig, String eePhilhealth, String adjDeductions,
            String totalDeductions, String netPay, String payStatus) {
        String[] replacement = {generateTransactionID(), empID, firstName, lastName, payDateFrom, payDateTo,
                basicSalary, hourlyRate, totalAllowances, hrsPerMonth, totalHrsWorked, adjEarnings,
                totalEarnings, tardinessAbsences, eeTax, eeSSS, eePagibig, eePhilhealth, adjDeductions,
                totalDeductions, netPay, payStatus};
        LocalRecordStore.update(LocalRecordStore.PAYROLL, rows -> {
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (Objects.equals(row[1], empID) && Objects.equals(row[4], payDateFrom)) {
                    replacement[0] = row[0];
                    rows.set(i, replacement);
                    return;
                }
            }
            rows.add(replacement);
        });
    }

    // Method to generate a 7-character alphanumeric transaction ID in Payroll
    private static String generateTransactionID() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 7; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    // Read the payroll CSV file
    public static List<String[]> readPayrollCSV() {
        // Delegate to DatabaseHandler instead of reading CSV
        return DatabaseHandler.readPayrollFromDatabase();
    }

    // Display Payroll Data (Debugging)
    public static void displayPayrollData() {
        List<String[]> payrollData = readPayrollCSV();
        if (payrollData.isEmpty()) {
            System.out.println("No payroll records found.");
        } else {
            System.out.println("Payroll Data:");
            for (String[] row : payrollData) {
                System.out.println(Arrays.toString(row));
            }
        }
    }

    public static void saveUpdatedPayrollData(List<String[]> payrollData) {
        LocalRecordStore.write(LocalRecordStore.PAYROLL, payrollData);
    }

    public static void updatePayrollCSV(String empID, String firstName, String lastName, String payDateFrom,
                                        String payDateTo, String basicSalary, String hourlyRate,
                                        String totalAllowances, String hrsPerMonth, String totalHrsWorked,
                                        String adjEarnings, String totalEarnings, String tardinessAbsences,
                                        String eeTax, String eeSSS, String eePagibig, String eePhilhealth,
                                        String adjDeductions, String totalDeductions, String netPay,
                                        String payStatus) {
        updatePayrollStatus(null, empID, payDateFrom, payDateTo, payStatus, "");
    }

    public static void updatePayrollStatus(String employeeID, String newStatus, String successMessage) {
        updatePayrollStatus(null, employeeID, null, null, newStatus, successMessage);
    }

    public static void updatePayrollStatus(String transactionID, String employeeID, String dateFrom, String dateTo,
            String newStatus, String successMessage) {
        LocalRecordStore.update(LocalRecordStore.PAYROLL, rows -> {
            String[] selected = null;
            for (String[] row : rows) {
                if (Objects.equals(row[1], employeeID) && (transactionID == null || Objects.equals(row[0], transactionID))
                        && (dateFrom == null || sameDate(row[4], dateFrom))
                        && (dateTo == null || sameDate(row[5], dateTo))) {
                    if (selected != null) { throw new PersistenceException("Multiple local payroll periods match. Select a specific transaction and payroll period."); }
                    selected = row;
                }
            }
            if (selected == null) { throw new PersistenceException("No local payroll record matches this transaction and payroll period. Database payroll rows do not contain payment approval status."); }
            selected[21] = newStatus;
        });
    }

    // To print only "Approved" payrolls
    public static void printApprovedPayroll(JTable tbl_Payroll, String dateFrom, String dateTo) {
        try {
            TableModel model = tbl_Payroll.getModel();
            List<String[]> approvedRows = new ArrayList<>();

            // Filter rows by Approved payStatus and date range
            for (int i = 0; i < model.getRowCount(); i++) {
                String payStatus = model.getValueAt(i, 21).toString(); // Column 21 = Pay Status
                String payDateFrom = model.getValueAt(i, 4).toString(); // Column 4 = Pay Date From
                String payDateTo = model.getValueAt(i, 5).toString(); // Column 5 = Pay Date To

                // Now it correctly checks the date range using dateFrom and dateTo
                if (payStatus.equalsIgnoreCase("Approved") && isWithinDateRange(payDateFrom, payDateTo, dateFrom, dateTo)) {
                    String[] rowData = {
                            model.getValueAt(i, 0).toString(), // Transaction ID
                            model.getValueAt(i, 1).toString(), // Emp ID
                            model.getValueAt(i, 2).toString(), // First Name
                            model.getValueAt(i, 3).toString(), // Last Name
                            model.getValueAt(i, 4).toString(), // Pay Date From
                            model.getValueAt(i, 5).toString(), // Pay Date To
                            model.getValueAt(i, 20).toString(), // Net Pay
                            model.getValueAt(i, 21).toString()  // Pay Status
                    };
                    approvedRows.add(rowData);
                }
            }

            if (approvedRows.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No approved payroll data found within the selected date range.", "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Print the filtered data
            printTable(approvedRows);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while printing: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Data Validation for Date Range in Payroll
    private static boolean isWithinDateRange(String payDateFrom, String payDateTo, String dateFrom, String dateTo) {
        try {
            // Ensure format matches CSV format exactly
            SimpleDateFormat csvDateFormat = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);

            // Parse CSV dates
            Date payStartDate = csvDateFormat.parse(payDateFrom);
            Date payEndDate = csvDateFormat.parse(payDateTo);

            // Parse selected dates from calendar
            Date selectedStartDate = csvDateFormat.parse(dateFrom);
            Date selectedEndDate = csvDateFormat.parse(dateTo);

            // Check if payroll period falls within selected range
            return (payStartDate.compareTo(selectedStartDate) >= 0 && payEndDate.compareTo(selectedEndDate) <= 0);

        } catch (ParseException e) {
            System.err.println("Error parsing dates: " + e.getMessage());
            return false;
        }
    }

    // Payroll Print Report
    private static void printTable(List<String[]> data) {
        try {
            StringBuilder printableData = new StringBuilder();
            printableData.append("Transaction ID | Emp ID | First Name | Last Name | Pay Date From | Pay Date To | Net Pay | Pay Status\n");
            printableData.append("-------------------------------------------------------------------------------------------\n");

            for (String[] row : data) {
                printableData.append(String.join(" | ", row)).append("\n");
            }

            JTextArea textArea = new JTextArea(printableData.toString());
            textArea.setColumns(80);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat format = job.defaultPage();
            format.setOrientation(PageFormat.LANDSCAPE);
            job.setPrintable(textArea.getPrintable(null, null), format);

            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(null, "Payroll report printed successfully!", "Print Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Print cancelled!", "Print Cancelled", JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(null, "Printing failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // For Logout
    public static void handleLogout(JFrame currentFrame) {
        int confirmLogout = JOptionPane.showConfirmDialog(currentFrame,
                "Do you want to logout?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmLogout == JOptionPane.YES_OPTION) {
            // Clear the user session
            EmpUserSession.getInstance().clearSession();

            // Open the login form
            new frm_Login().setVisible(true);

            // Close the current frame
            currentFrame.dispose();
        }
    }

    // Validate First Name & Last Name in Employee Records (Auto-Capitalize, Max 50 characters)
    public static String capitalizeWords(String input, int maxLength) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder capitalizedString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedString.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        String result = capitalizedString.toString().trim();
        return (result.length() > maxLength) ? result.substring(0, maxLength) : result;
    }

    // Validate Address (Auto-Capitalize, Max 100 characters)
    public static String validateAddress(String address) {
        return capitalizeWords(address, 100);
    }

    // Validate Phone Number (Only numeric and hyphen, Max 11 characters)
    public static boolean isValidPhone(String phoneNo) {
        return phoneNo.matches("^(\\d+-?)+\\d$") && phoneNo.length() <= 11;
    }

    // Validate Birthdate (Format: DD-MMM-YY)
    public static String formatBirthdate(String birthdate) {
        String birthdatePattern = "^\\d{2}-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)-\\d{2}$";
        Pattern pattern = Pattern.compile(birthdatePattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(birthdate);

        if (!matcher.matches()) {
            return null; // Invalid format, return null instead of boolean
        }

        // Convert month to "Mmm" format (First letter uppercase, rest lowercase)
        String[] parts = birthdate.split("-");
        String formattedMonth = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1).toLowerCase();

        return parts[0] + "-" + formattedMonth + "-" + parts[2]; // Return formatted birthdate
    }

    // Helper method to safely parse LocalDate values in Attendance
    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) { throw new PersistenceException("A local attendance record has no date."); }

        List<String> patterns = Arrays.asList("M/d/yyyy", "MM/dd/yyyy", "d/M/yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MMM-yy"); //JOYCE--- added "dd-MMM-yy"
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
            } catch (Exception ignored) {
            }
        }

        throw new PersistenceException("A local attendance record has an invalid date.");
    }

    // Helper method to safely parse LocalTime values
    private static LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) { throw new PersistenceException("A local attendance record has no time."); }

        List<String> patterns = Arrays.asList("H:mm:ss", "HH:mm");
        for (String pattern : patterns) {
            try {
                return LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
            } catch (Exception ignored) {
            }
        }

        throw new PersistenceException("A local attendance record has an invalid time.");
    }

    // Helper method to safely parse double values
    private static double parseDouble(String value) {
        try {
            return new BigDecimal(value.trim()).doubleValue();
        } catch (NumberFormatException e) {
            throw new PersistenceException("A local attendance record has an invalid number.");
        }
    }
}


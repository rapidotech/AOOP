package oop_motorph;

import java.sql.*;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static Connection getConnection() {
        return DatabaseConfig.getInstance().getConnection();
    }

    // User Credentials
    public static Map<String, String[]> loadCredentials() {
        Map<String, String[]> credentials = new HashMap<>();
        String query = "SELECT username, password, role, first_name, last_name FROM Users";

        try (Connection conn = getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                credentials.put(username, new String[]{password, role, firstName, lastName});
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return credentials;
    }

    public static final class AuthenticationResult {
        private final EmpDetails employee;
        private final String role;
        private AuthenticationResult(EmpDetails employee, String role) {
            this.employee = employee;
            this.role = role;
        }
        public EmpDetails getEmployee() { return employee; }
        public String getRole() { return role; }
    }

    /** Preserves the existing SHA-256 storage format without loading every account. */
    public static AuthenticationResult authenticate(String username, String passwordHash) {
        if (username == null || username.isBlank() || passwordHash == null) { return null; }
        String role;
        try (Connection conn = getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn,
                     "SELECT password, role FROM Users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) { return null; }
                String storedHash = rs.getString("password");
                if (storedHash == null || !java.security.MessageDigest.isEqual(
                        storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        passwordHash.getBytes(java.nio.charset.StandardCharsets.UTF_8))) { return null; }
                role = rs.getString("role");
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("authenticate the account", e);
        }
        List<EmpDetails> employees = getEmployeeData(username);
        return employees.isEmpty() ? null : new AuthenticationResult(employees.get(0), role);
    }

    // Employee Operations
    public static List<EmpDetails> getEmployeeData() {
        return getEmployeeData(null);
    }

    private static List<EmpDetails> getEmployeeData(String employeeID) {
        List<EmpDetails> employees = new ArrayList<>();
        String query = "SELECT e.EmployeeID, e.FirstName, e.LastName, e.Birthday, " +
                      "a.StreetAddress, a.City, a.Region, " +
                      "e.PhoneNumber, es.StatusName, p.PositionName, " +
                      "CONCAT(sup.FirstName, ' ', sup.LastName) as SupervisorName " +
                      "FROM employees e " +
                      "LEFT JOIN addresses a ON e.AddressID = a.AddressID " +
                      "LEFT JOIN employeestatuses es ON e.StatusID = es.StatusID " +
                      "LEFT JOIN positions p ON e.PositionID = p.PositionID " +
                      "LEFT JOIN employees sup ON e.ImmediateSupervisorID = sup.EmployeeID " +
                      (employeeID == null ? "" : "WHERE e.EmployeeID = ? ") +
                      "ORDER BY e.EmployeeID";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query)) {
            if (employeeID != null) { stmt.setString(1, employeeID); }
            try (ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Build address string
                String address = "";
                String street = rs.getString("StreetAddress");
                String city = rs.getString("City");
                String region = rs.getString("Region");

                if (street != null && !street.isEmpty()) {
                    address = street;
                }
                if (city != null && !city.isEmpty()) {
                    address += (address.isEmpty() ? "" : ", ") + city;
                }
                if (region != null && !region.isEmpty()) {
                    address += (address.isEmpty() ? "" : ", ") + region;
                }

                EmpDetails emp = new EmpDetails(
                        rs.getString("EmployeeID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Birthday"),
                        address.isEmpty() ? "Address not available" : address,
                        rs.getString("PhoneNumber"),
                        rs.getString("StatusName") != null ? rs.getString("StatusName") : "Status not available",
                        rs.getString("PositionName") != null ? rs.getString("PositionName") : "Position not available",
                        rs.getString("SupervisorName") != null ? rs.getString("SupervisorName") : "No supervisor"
                );
                employees.add(emp);
            }
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return employees;
    }

    public static boolean employeeExists(String empID, String existingEmpID) {
        if (empID == null || empID.isBlank()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        boolean excludingCurrent = existingEmpID != null && !existingEmpID.isBlank();
        String query = "SELECT COUNT(*) FROM employees WHERE EmployeeID = ?"
                + (excludingCurrent ? " AND EmployeeID <> ?" : "");
        try (Connection conn = getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query)) {
            stmt.setString(1, empID);
            if (excludingCurrent) { stmt.setString(2, existingEmpID); }
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) { throw new SQLException("Employee count was not returned."); }
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("check the employee ID", e);
        }
    }

    public static void addEmployee(EmpDetails newEmp) {
        PersistenceSupport.inTransaction("add the employee", conn -> insertEmployee(conn, newEmp));
    }

    private static void insertEmployee(Connection conn, EmpDetails newEmp) throws SQLException {
        // First, insert address if not exists
        int addressId = insertOrGetAddressId(conn, newEmp.getAddress());

        // Get or create status ID
        int statusId = getOrCreateStatusId(conn, newEmp.getEmployeeStatus());

        // Get or create position ID
        int positionId = getOrCreatePositionId(conn, newEmp.getPosition());

        // Find supervisor ID if exists
        Integer supervisorId = getSupervisorId(conn, newEmp.getImmediateSupervisor());

        // Insert into employees table
        String empQuery = "INSERT INTO employees (EmployeeID, FirstName, LastName, Birthday, AddressID, PhoneNumber, StatusID, PositionID, DepartmentID, ImmediateSupervisorID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";
        try (PreparedStatement empStmt = PersistenceSupport.prepareStatement(conn, empQuery)) {
            empStmt.setInt(1, Integer.parseInt(newEmp.getEmpID()));
            empStmt.setString(2, newEmp.getFirstName());
            empStmt.setString(3, newEmp.getLastName());

            // Handle empty birthday properly - provide default date if empty
            String birthdate = newEmp.getBirthdate();
            if (birthdate == null || birthdate.trim().isEmpty()) {
                empStmt.setString(4, "1990-01-01"); // Default birthday
            } else {
                empStmt.setString(4, birthdate);
            }

            empStmt.setInt(5, addressId);
            empStmt.setString(6, newEmp.getPhoneNumber());
            empStmt.setInt(7, statusId);
            empStmt.setInt(8, positionId);
            empStmt.setObject(9, supervisorId);
            PersistenceSupport.requireOneRow(empStmt.executeUpdate());
        }

        // Insert default compensation record (exclude generated columns)
        String compQuery = "INSERT INTO compensation (EmployeeID, BasicSalary, RiceSubsidy, PhoneAllowance, ClothingAllowance) VALUES (?, 15000, 1500, 500, 500)";
        try (PreparedStatement compStmt = PersistenceSupport.prepareStatement(conn, compQuery)) {
            compStmt.setInt(1, Integer.parseInt(newEmp.getEmpID()));
            PersistenceSupport.requireOneRow(compStmt.executeUpdate());
        }

        // Insert default government IDs record
        String govQuery = "INSERT INTO governmentids (EmployeeID, SSSNumber, PhilHealthNumber, TINNumber, PagIBIGNumber) VALUES (?, '', '', '', '')";
        try (PreparedStatement govStmt = PersistenceSupport.prepareStatement(conn, govQuery)) {
            govStmt.setInt(1, Integer.parseInt(newEmp.getEmpID()));
            PersistenceSupport.requireOneRow(govStmt.executeUpdate());
        }
    }

    public static void updateEmployee(EmpDetails updatedEmp) {
        PersistenceSupport.inTransaction("update the employee", conn -> {

            // Update address
            int addressId = insertOrGetAddressId(conn, updatedEmp.getAddress());

            // Get or create status ID
            int statusId = getOrCreateStatusId(conn, updatedEmp.getEmployeeStatus());

            // Get or create position ID
            int positionId = getOrCreatePositionId(conn, updatedEmp.getPosition());

            // Find supervisor ID if exists
            Integer supervisorId = getSupervisorId(conn, updatedEmp.getImmediateSupervisor());

            // Update employees table
            String empQuery = "UPDATE employees SET FirstName = ?, LastName = ?, Birthday = ?, AddressID = ?, PhoneNumber = ?, StatusID = ?, PositionID = ?, ImmediateSupervisorID = ? WHERE EmployeeID = ?";
            try (PreparedStatement empStmt = PersistenceSupport.prepareStatement(conn, empQuery)) {
                empStmt.setString(1, updatedEmp.getFirstName());
                empStmt.setString(2, updatedEmp.getLastName());

                // Handle empty birthday properly - provide default date if empty
                String birthdate = updatedEmp.getBirthdate();
                if (birthdate == null || birthdate.trim().isEmpty()) {
                    empStmt.setString(3, "1990-01-01"); // Default birthday
                } else {
                    empStmt.setString(3, birthdate);
                }

                empStmt.setInt(4, addressId);
                empStmt.setString(5, updatedEmp.getPhoneNumber());
                empStmt.setInt(6, statusId);
                empStmt.setInt(7, positionId);
                empStmt.setObject(8, supervisorId);
                empStmt.setInt(9, Integer.parseInt(updatedEmp.getEmpID()));
                PersistenceSupport.requireOneRow(empStmt.executeUpdate());
            }

        });
    }

    public static void deleteEmployee(String empID) {
        PersistenceSupport.inTransaction("delete the employee", conn -> {

            int employeeId = Integer.parseInt(empID);

            // Delete from related tables first (foreign key constraints)
            String[] deleteQueries = {
                    "DELETE FROM governmentids WHERE EmployeeID = ?",
                    "DELETE FROM compensation WHERE EmployeeID = ?",
                    "DELETE FROM attendance WHERE EmployeeID = ?",
                    "DELETE FROM employees WHERE EmployeeID = ?"
            };

            for (String query : deleteQueries) {
                try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query)) {
                    stmt.setInt(1, employeeId);
                    int rows = stmt.executeUpdate();
                    if (query.contains("FROM employees ")) { PersistenceSupport.requireOneRow(rows); }
                }
            }

        });
    }

    // Salary Operations
    public static List<EmpSalaryDetails> getSalaryData() {
        List<EmpSalaryDetails> salaryList = new ArrayList<>();
        String query = "SELECT e.EmployeeID, e.FirstName, e.LastName, " +
                      "g.SSSNumber, g.PhilHealthNumber, g.TINNumber, g.PagIBIGNumber, " +
                      "c.BasicSalary, c.RiceSubsidy, c.PhoneAllowance, c.ClothingAllowance, " +
                      "c.GrossSemimonthly, c.HourlyRate " +
                      "FROM employees e " +
                      "LEFT JOIN governmentids g ON e.EmployeeID = g.EmployeeID " +
                      "LEFT JOIN compensation c ON e.EmployeeID = c.EmployeeID " +
                      "ORDER BY e.EmployeeID";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EmpSalaryDetails salary = new EmpSalaryDetails(
                        rs.getString("EmployeeID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("SSSNumber") != null ? rs.getString("SSSNumber") : "",
                        rs.getString("PhilHealthNumber") != null ? rs.getString("PhilHealthNumber") : "",
                        rs.getString("TINNumber") != null ? rs.getString("TINNumber") : "",
                        rs.getString("PagIBIGNumber") != null ? rs.getString("PagIBIGNumber") : "",
                        rs.getDouble("BasicSalary"),
                        rs.getDouble("RiceSubsidy"),
                        rs.getDouble("PhoneAllowance"),
                        rs.getDouble("ClothingAllowance"),
                        rs.getDouble("GrossSemimonthly"),
                        rs.getDouble("HourlyRate")
                );
                salaryList.add(salary);
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return salaryList;
    }

    public static void addEmployeeSalary(EmpSalaryDetails salary) {
        Objects.requireNonNull(salary, "Salary details are required.");
        EmpDetails employee = new EmpDetails(salary.getEmpID(), salary.getFirstName(), salary.getLastName(),
                "", "", "", "Active", "General", "");
        PersistenceSupport.inTransaction("add the employee and salary", conn -> {
            insertEmployee(conn, employee);
            writeEmployeeSalary(conn, salary);
        });
    }

    public static void updateEmployeeSalary(EmpSalaryDetails updatedSalary) {
        PersistenceSupport.inTransaction("update employee salary", conn -> writeEmployeeSalary(conn, updatedSalary));
    }

    private static void writeEmployeeSalary(Connection conn, EmpSalaryDetails updatedSalary) throws SQLException {

        // Update employee basic information
        String empQuery = "UPDATE employees SET FirstName = ?, LastName = ? WHERE EmployeeID = ?";
        try (PreparedStatement empStmt = PersistenceSupport.prepareStatement(conn, empQuery)) {
            empStmt.setString(1, updatedSalary.getFirstName());
            empStmt.setString(2, updatedSalary.getLastName());
            empStmt.setString(3, updatedSalary.getEmpID());
            PersistenceSupport.requireOneRow(empStmt.executeUpdate());
        }

        // Update government IDs
        String govQuery = "UPDATE governmentids SET SSSNumber = ?, PhilHealthNumber = ?, TINNumber = ?, PagIBIGNumber = ? WHERE EmployeeID = ?";
        try (PreparedStatement govStmt = PersistenceSupport.prepareStatement(conn, govQuery)) {
            govStmt.setString(1, updatedSalary.getSssNo());
            govStmt.setString(2, updatedSalary.getPhilhealthNo());
            govStmt.setString(3, updatedSalary.getTinNo());
            govStmt.setString(4, updatedSalary.getPagibigNo());
            govStmt.setString(5, updatedSalary.getEmpID());
            PersistenceSupport.requireOneRow(govStmt.executeUpdate());
        }

        // Update compensation details (exclude GrossSemimonthly and HourlyRate as they are generated columns)
        String compQuery = "UPDATE compensation SET BasicSalary = ?, RiceSubsidy = ?, PhoneAllowance = ?, ClothingAllowance = ? WHERE EmployeeID = ?";
        try (PreparedStatement compStmt = PersistenceSupport.prepareStatement(conn, compQuery)) {
            compStmt.setDouble(1, updatedSalary.getBasicSalary());
            compStmt.setDouble(2, updatedSalary.getRiceSubsidy());
            compStmt.setDouble(3, updatedSalary.getPhoneAllowance());
            compStmt.setDouble(4, updatedSalary.getClothingAllowance());
            compStmt.setString(5, updatedSalary.getEmpID());
            PersistenceSupport.requireOneRow(compStmt.executeUpdate());
        }

    }

    // Attendance Operations
    public static List<EmpAttLeave> getAttendanceData() {
        List<EmpAttLeave> attendanceList = new ArrayList<>();
        String query = "SELECT a.EmployeeID, e.FirstName, e.LastName, " +
                      "es.StatusName, p.PositionName, " +
                      "CONCAT(sup.FirstName, ' ', sup.LastName) as SupervisorName, " +
                      "a.AttendanceDate, a.AttendanceDate, a.LoginTime, a.LogoutTime, " +
                      "a.TotalHours, 0 as OvertimeHours, 'Regular' as AttendanceType, 'Present' as AttendanceStatus " +
                      "FROM attendance a " +
                      "JOIN employees e ON a.EmployeeID = e.EmployeeID " +
                      "LEFT JOIN employeestatuses es ON e.StatusID = es.StatusID " +
                      "LEFT JOIN positions p ON e.PositionID = p.PositionID " +
                      "LEFT JOIN employees sup ON e.ImmediateSupervisorID = sup.EmployeeID " +
                      "ORDER BY a.EmployeeID, a.AttendanceDate";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EmpAttLeave attendance = new EmpAttLeave(
                        rs.getString("EmployeeID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("StatusName") != null ? rs.getString("StatusName") : "Status not available",
                        rs.getString("PositionName") != null ? rs.getString("PositionName") : "Position not available",
                        rs.getString("SupervisorName") != null ? rs.getString("SupervisorName") : "No supervisor",
                        rs.getDate("AttendanceDate") != null ? rs.getDate("AttendanceDate").toLocalDate() : null,
                        rs.getDate("AttendanceDate") != null ? rs.getDate("AttendanceDate").toLocalDate() : null,
                        rs.getTime("LoginTime") != null ? rs.getTime("LoginTime").toLocalTime() : null,
                        rs.getTime("LogoutTime") != null ? rs.getTime("LogoutTime").toLocalTime() : null,
                        rs.getDouble("TotalHours"),
                        rs.getDouble("OvertimeHours"),
                        rs.getString("AttendanceType"),
                        rs.getString("AttendanceStatus"),
                        0.0, // vl_count - not available in normalized schema
                        0.0, // vl_used - not available in normalized schema
                        0.0, // vl_balance - not available in normalized schema
                        0.0, // sl_count - not available in normalized schema
                        0.0, // sl_used - not available in normalized schema
                        0.0  // sl_balance - not available in normalized schema
                );
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return attendanceList;
    }

    public static void saveAttendanceRequest(EmpAttLeave empAttLeave) {
        CSVHandler.saveAttendanceRequest(empAttLeave);
    }

    public static List<String[]> readPayrollFromDatabase() {
        List<String[]> payrollData = new ArrayList<>();
        String query = "SELECT * FROM Payroll ORDER BY EmployeeID, PayrollPeriodStartDate";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Optionally include header row
            String[] header = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                header[i - 1] = meta.getColumnLabel(i);
            }
            payrollData.add(header);

            // Fetch each row as String[]
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                payrollData.add(row);
            }

        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return payrollData;
    }

    // Payroll Operations
    public static List<String[]> getPayrollData() {
        List<String[]> payrollList = new ArrayList<>();

        // Query to get payroll data from the Payroll table - using only actual database columns
        String query = """
            SELECT
                p.EmployeeID,
                e.FirstName,
                e.LastName,
                pos.PositionName,
                d.DepartmentName,
                p.Gross as total_earnings,
                g.SSSNumber,
                p.SSSContribution as ee_sss,
                g.PhilHealthNumber,
                p.PhilHealthContribution as ee_philhealth,
                g.PagIBIGNumber,
                p.PagIBIGContribution as ee_pagibig,
                g.TINNumber,
                p.Tax as ee_tax,
                p.NetPay as net_pay,
                p.PayrollPeriodStartDate
            FROM Payroll p
            LEFT JOIN employees e ON p.EmployeeID = e.EmployeeID
            LEFT JOIN governmentids g ON e.EmployeeID = g.EmployeeID
            LEFT JOIN positions pos ON e.PositionID = pos.PositionID
            LEFT JOIN departments d ON e.DepartmentID = d.DepartmentID
            WHERE p.EmployeeID IS NOT NULL
            ORDER BY p.EmployeeID DESC
            """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            // Fetch each row and map to the desired column format
            while (rs.next()) {
                String[] row = new String[15]; // 14 display columns + 1 hidden date column for filtering

                // Map joined table data to desired format
                row[0] = rs.getString("EmployeeID") != null ? rs.getString("EmployeeID") : ""; // Employee No

                // Combine FirstName and LastName
                String firstName = rs.getString("FirstName") != null ? rs.getString("FirstName") : "";
                String lastName = rs.getString("LastName") != null ? rs.getString("LastName") : "";
                row[1] = (firstName + " " + lastName).trim(); // Employee Full Name

                row[2] = rs.getString("PositionName") != null ? rs.getString("PositionName") : ""; // Position
                row[3] = rs.getString("DepartmentName") != null ? rs.getString("DepartmentName") : ""; // Department

                // Use total_earnings as Gross Income
                double grossIncome = rs.getDouble("total_earnings");
                row[4] = String.format("%.2f", grossIncome); // Gross Income

                row[5] = rs.getString("SSSNumber") != null ? rs.getString("SSSNumber") : ""; // Social Security No.

                // Use ee_sss as Social Security Contribution
                double sssContrib = rs.getDouble("ee_sss");
                row[6] = String.format("%.2f", sssContrib); // Social Security Contribution

                row[7] = rs.getString("PhilHealthNumber") != null ? rs.getString("PhilHealthNumber") : ""; // Philhealth No.

                // Use ee_philhealth as Philhealth Contribution
                double philhealthContrib = rs.getDouble("ee_philhealth");
                row[8] = String.format("%.2f", philhealthContrib); // Philhealth Contribution

                row[9] = rs.getString("PagIBIGNumber") != null ? rs.getString("PagIBIGNumber") : ""; // Pag-ibig No.

                // Use ee_pagibig as Pag-ibig Contribution
                double pagibigContrib = rs.getDouble("ee_pagibig");
                row[10] = String.format("%.2f", pagibigContrib); // Pag-ibig Contribution

                row[11] = rs.getString("TINNumber") != null ? rs.getString("TINNumber") : ""; // TIN

                // Use ee_tax as Withholding Tax
                double withholdingTax = rs.getDouble("ee_tax");
                row[12] = String.format("%.2f", withholdingTax); // Withholding Tax

                // Use net_pay
                double netPay = rs.getDouble("net_pay");
                row[13] = String.format("%.2f", netPay); // Net Pay

                // Add the date as the 15th column (index 14) for filtering - not displayed
                Date payrollDate = rs.getDate("PayrollPeriodStartDate");
                row[14] = payrollDate != null ? payrollDate.toString() : "";

                payrollList.add(row);
            }

        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return payrollList;
    }

    public static String[] getPayrollColumnNames() {
        // Return the fixed column names that match the image + hidden date column
        return new String[]{
            "Employee No", "Employee Full Name", "Position", "Department", "Gross Income",
            "Social Security No.", "Social Security Contribution", "Philhealth No.",
            "Philhealth Contribution", "Pag-ibig No.", "Pag-ibig Contribution",
            "TIN", "Withholding Tax", "Net Pay", "PayrollPeriodStartDate"
        };
    }

    // Utility methods for validation
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

    public static String validateAddress(String address) {
        return capitalizeWords(address, 100);
    }

    public static boolean isValidPhone(String phoneNo) {
        return phoneNo.matches("^(\\d+-?)+\\d$") && phoneNo.length() <= 11;
    }

    // Helper methods for normalized database operations

    private static int insertOrGetAddressId(Connection conn, String address) throws SQLException {
        if (address == null || address.trim().isEmpty() || address.equals("Address not available")) {
            address = "Unknown Address";
        }

        // Try to find existing address
        String selectQuery = "SELECT AddressID FROM addresses WHERE StreetAddress = ?";
        try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, selectQuery)) {
            stmt.setString(1, address);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("AddressID");
                }
            }
        }

        // Insert new address
        String insertQuery = "INSERT INTO addresses (StreetAddress, City, Region) VALUES (?, 'Unknown', 'Unknown')";
        try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, address);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Address insert returned no generated ID.");
    }

    private static int getOrCreateStatusId(Connection conn, String statusName) throws SQLException {
        if (statusName == null || statusName.trim().isEmpty() || statusName.equals("Status not available")) {
            statusName = "Regular";
        }

        // Try to find existing status
        String selectQuery = "SELECT StatusID FROM employeestatuses WHERE StatusName = ?";
        try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, selectQuery)) {
            stmt.setString(1, statusName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("StatusID");
                }
            }
        }

        // Insert new status
        String insertQuery = "INSERT INTO employeestatuses (StatusName) VALUES (?)";
        try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, statusName);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Status insert returned no generated ID.");
    }

    private static int getOrCreatePositionId(Connection conn, String positionName) throws SQLException {
        if (positionName == null || positionName.trim().isEmpty() || positionName.equals("Position not available")) {
            positionName = "General";
        }

        // Try to find existing position
        String selectQuery = "SELECT PositionID FROM positions WHERE PositionName = ?";
        try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, selectQuery)) {
            stmt.setString(1, positionName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("PositionID");
                }
            }
        }

        // Insert new position
        String insertQuery = "INSERT INTO positions (PositionName) VALUES (?)";
        try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, positionName);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Position insert returned no generated ID.");
    }

    private static Integer getSupervisorId(Connection conn, String supervisorName) throws SQLException {
        if (supervisorName == null || supervisorName.trim().isEmpty() || supervisorName.equals("No supervisor") || supervisorName.equals("N/A")) {
            return null;
        }
        // If supervisorName is numeric, treat as EmployeeID
        if (supervisorName.matches("\\d+")) {
            return Integer.parseInt(supervisorName);
        }
        // Otherwise, fallback to name lookup (legacy)
        String[] names = supervisorName.split("\\s+");
        if (names.length < 2) {
            return null;
        }
        String firstName = names[0];
        String lastName = names[names.length - 1];
        String selectQuery = "SELECT EmployeeID FROM employees WHERE FirstName = ? AND LastName = ?";
        try (PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, selectQuery)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("EmployeeID");
                }
            }
        }
        return null; // No supervisor found
    }

    public static int getNextEmployeeId() {
        String query = "SELECT MAX(EmployeeID) FROM employees";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int maxId = rs.getInt(1);
                return maxId + 1;
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return 1; // Default to 1 if no employees exist or error occurs
    }

    // Helper methods for normalized database operations

    public static List<String> getAllSupervisors() {
        List<String> supervisors = new ArrayList<>();
        supervisors.add("N/A"); // Option for no supervisor

        String query = "SELECT CONCAT(FirstName, ' ', LastName) as FullName " +
                      "FROM employees " +
                      "WHERE EmployeeID IS NOT NULL " +
                      "ORDER BY FirstName, LastName";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fullName = rs.getString("FullName");
                if (fullName != null && !fullName.trim().isEmpty()) {
                    supervisors.add(fullName);
                }
            }
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }

        return supervisors;
    }

    // Debug method to check supervisor relationships
    public static void debugSupervisorData() {
        String query = "SELECT e.EmployeeID, e.FirstName, e.LastName, " +
                      "e.ImmediateSupervisorID, " +
                      "CONCAT(sup.FirstName, ' ', sup.LastName) as SupervisorName " +
                      "FROM employees e " +
                      "LEFT JOIN employees sup ON e.ImmediateSupervisorID = sup.EmployeeID " +
                      "ORDER BY e.EmployeeID";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = PersistenceSupport.prepareStatement(conn, query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("=== SUPERVISOR DEBUG INFO ===");
            while (rs.next()) {
                System.out.println("Employee: " + rs.getString("EmployeeID") + " - " +
                                 rs.getString("FirstName") + " " + rs.getString("LastName") +
                                 " | Supervisor ID: " + rs.getString("ImmediateSupervisorID") +
                                 " | Supervisor Name: " + rs.getString("SupervisorName"));
            }
            System.out.println("=== END DEBUG INFO ===");
        } catch (SQLException e) {
            throw PersistenceException.databaseFailure("load database records", e);
        }
    }
}

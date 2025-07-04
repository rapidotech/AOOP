package oop_motorph;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Date;
import javax.swing.JOptionPane;

public class DatabaseHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Connection getConnection() {
        return DatabaseConfig.getInstance().getConnection();
    }

    // User Credentials
    public static Map<String, String[]> loadCredentials() {
        Map<String, String[]> credentialsMap = new HashMap<>();
        String query = "SELECT username, password, role, first_name, last_name FROM users";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String[] userDetails = {
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                };
                credentialsMap.put(username, userDetails);
            }
        } catch (SQLException e) {
            System.err.println("Error loading credentials: " + e.getMessage());
            e.printStackTrace();
        }

        return credentialsMap;
    }

    // Employee Operations
    public static List<EmpDetails> getEmployeeData() {
        List<EmpDetails> employees = new ArrayList<>();
        String query = "SELECT * FROM employees ORDER BY emp_id";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EmpDetails emp = new EmpDetails(
                        rs.getString("emp_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("birthday"),
                        rs.getString("address"),
                        rs.getString("phone_number"),
                        rs.getString("status"),
                        rs.getString("position"),
                        rs.getString("immediate_supervisor")
                );
                employees.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("Error getting employee data: " + e.getMessage());
            e.printStackTrace();
        }

        return employees;
    }

    public static boolean employeeExists(String empID, String existingEmpID) {
        String query = "SELECT COUNT(*) FROM employees WHERE emp_id = ? AND emp_id != ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, empID);
            stmt.setString(2, existingEmpID != null ? existingEmpID : "");

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking employee existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static void addEmployee(EmpDetails newEmp) {
        Connection conn = DatabaseConfig.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            // Insert into employees table
            String empQuery = "INSERT INTO employees (emp_id, first_name, last_name, birthday, address, phone_number, status, position, immediate_supervisor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement empStmt = conn.prepareStatement(empQuery)) {
                empStmt.setString(1, newEmp.getEmpID());
                empStmt.setString(2, newEmp.getFirstName());
                empStmt.setString(3, newEmp.getLastName());
                empStmt.setString(4, newEmp.getBirthdate());
                empStmt.setString(5, newEmp.getAddress());
                empStmt.setString(6, newEmp.getPhoneNumber());
                empStmt.setString(7, newEmp.getEmployeeStatus());
                empStmt.setString(8, newEmp.getPosition());
                empStmt.setString(9, newEmp.getImmediateSupervisor());
                empStmt.executeUpdate();
            }

            // Insert into salary table with default values
            String salaryQuery = "INSERT INTO salary (emp_id, first_name, last_name, sss_no, philhealth_no, tin_no, pagibig_no, basic_salary, rice_allowance, phone_allowance, clothing_allowance, gross_semi, hourly_rate) VALUES (?, ?, ?, '', '', '', '', 0, 0, 0, 0, 0, 0)";
            try (PreparedStatement salaryStmt = conn.prepareStatement(salaryQuery)) {
                salaryStmt.setString(1, newEmp.getEmpID());
                salaryStmt.setString(2, newEmp.getFirstName());
                salaryStmt.setString(3, newEmp.getLastName());
                salaryStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Employee added successfully");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error adding employee: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public static void updateEmployee(EmpDetails updatedEmp) {
        Connection conn = DatabaseConfig.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            // Update employees table
            String empQuery = "UPDATE employees SET first_name = ?, last_name = ?, birthday = ?, address = ?, phone_number = ?, status = ?, position = ?, immediate_supervisor = ? WHERE emp_id = ?";
            try (PreparedStatement empStmt = conn.prepareStatement(empQuery)) {
                empStmt.setString(1, updatedEmp.getFirstName());
                empStmt.setString(2, updatedEmp.getLastName());
                empStmt.setString(3, updatedEmp.getBirthdate());
                empStmt.setString(4, updatedEmp.getAddress());
                empStmt.setString(5, updatedEmp.getPhoneNumber());
                empStmt.setString(6, updatedEmp.getEmployeeStatus());
                empStmt.setString(7, updatedEmp.getPosition());
                empStmt.setString(8, updatedEmp.getImmediateSupervisor());
                empStmt.setString(9, updatedEmp.getEmpID());
                empStmt.executeUpdate();
            }

            // Update related tables
            String salaryQuery = "UPDATE salary SET first_name = ?, last_name = ? WHERE emp_id = ?";
            try (PreparedStatement salaryStmt = conn.prepareStatement(salaryQuery)) {
                salaryStmt.setString(1, updatedEmp.getFirstName());
                salaryStmt.setString(2, updatedEmp.getLastName());
                salaryStmt.setString(3, updatedEmp.getEmpID());
                salaryStmt.executeUpdate();
            }

            String attQuery = "UPDATE attendance SET first_name = ?, last_name = ?, status = ?, position = ?, immediate_supervisor = ? WHERE emp_id = ?";
            try (PreparedStatement attStmt = conn.prepareStatement(attQuery)) {
                attStmt.setString(1, updatedEmp.getFirstName());
                attStmt.setString(2, updatedEmp.getLastName());
                attStmt.setString(3, updatedEmp.getEmployeeStatus());
                attStmt.setString(4, updatedEmp.getPosition());
                attStmt.setString(5, updatedEmp.getImmediateSupervisor());
                attStmt.setString(6, updatedEmp.getEmpID());
                attStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Employee updated successfully");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error updating employee: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public static void deleteEmployee(String empID) {
        Connection conn = DatabaseConfig.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            // Delete from related tables first (foreign key constraints)
            String[] deleteQueries = {
                    "DELETE FROM payroll WHERE emp_id = ?",
                    "DELETE FROM attendance WHERE emp_id = ?",
                    "DELETE FROM salary WHERE emp_id = ?",
                    "DELETE FROM employees WHERE emp_id = ?"
            };

            for (String query : deleteQueries) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, empID);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("Employee deleted successfully");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error deleting employee: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    // Salary Operations
    public static List<EmpSalaryDetails> getSalaryData() {
        List<EmpSalaryDetails> salaryList = new ArrayList<>();
        String query = "SELECT * FROM salary ORDER BY emp_id";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EmpSalaryDetails salary = new EmpSalaryDetails(
                        rs.getString("emp_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("sss_no"),
                        rs.getString("philhealth_no"),
                        rs.getString("tin_no"),
                        rs.getString("pagibig_no"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("rice_allowance"),
                        rs.getDouble("phone_allowance"),
                        rs.getDouble("clothing_allowance"),
                        rs.getDouble("gross_semi"),
                        rs.getDouble("hourly_rate")
                );
                salaryList.add(salary);
            }
        } catch (SQLException e) {
            System.err.println("Error getting salary data: " + e.getMessage());
            e.printStackTrace();
        }

        return salaryList;
    }

    public static void updateEmployeeSalary(EmpSalaryDetails updatedSalary) {
        String query = "UPDATE salary SET first_name = ?, last_name = ?, sss_no = ?, philhealth_no = ?, tin_no = ?, pagibig_no = ?, basic_salary = ?, rice_allowance = ?, phone_allowance = ?, clothing_allowance = ?, gross_semi = ?, hourly_rate = ? WHERE emp_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, updatedSalary.getFirstName());
            stmt.setString(2, updatedSalary.getLastName());
            stmt.setString(3, updatedSalary.getSssNo());
            stmt.setString(4, updatedSalary.getPhilhealthNo());
            stmt.setString(5, updatedSalary.getTinNo());
            stmt.setString(6, updatedSalary.getPagibigNo());
            stmt.setDouble(7, updatedSalary.getBasicSalary());
            stmt.setDouble(8, updatedSalary.getRiceSubsidy());
            stmt.setDouble(9, updatedSalary.getPhoneAllowance());
            stmt.setDouble(10, updatedSalary.getClothingAllowance());
            stmt.setDouble(11, updatedSalary.getGrossSemi());
            stmt.setDouble(12, updatedSalary.getHourlyRate());
            stmt.setString(13, updatedSalary.getEmpID());

            stmt.executeUpdate();
            System.out.println("Salary updated successfully");

        } catch (SQLException e) {
            System.err.println("Error updating salary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Attendance Operations
    public static List<EmpAttLeave> getAttendanceData() {
        List<EmpAttLeave> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance ORDER BY emp_id, date_from";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate dateFrom = rs.getDate("date_from") != null ?
                        rs.getDate("date_from").toLocalDate() : LocalDate.of(2000, 1, 1);
                LocalDate dateTo = rs.getDate("date_to") != null ?
                        rs.getDate("date_to").toLocalDate() : LocalDate.of(2000, 1, 1);
                LocalTime timeIn = rs.getTime("time_in") != null ?
                        rs.getTime("time_in").toLocalTime() : LocalTime.of(8, 0);
                LocalTime timeOut = rs.getTime("time_out") != null ?
                        rs.getTime("time_out").toLocalTime() : LocalTime.of(17, 0);

                EmpAttLeave attendance = new EmpAttLeave(
                        rs.getString("emp_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("employee_status"),
                        rs.getString("position"),
                        rs.getString("immediate_supervisor"),
                        dateFrom,
                        dateTo,
                        timeIn,
                        timeOut,
                        rs.getDouble("hours_worked"),
                        rs.getDouble("duration"),
                        rs.getString("attendance_type"),
                        rs.getString("attendance_status"),
                        rs.getDouble("vl_count"),
                        rs.getDouble("vl_used"),
                        rs.getDouble("vl_balance"),
                        rs.getDouble("sl_count"),
                        rs.getDouble("sl_used"),
                        rs.getDouble("sl_balance")
                );
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            System.err.println("Error getting attendance data: " + e.getMessage());
            e.printStackTrace();
        }

        return attendanceList;
    }

    public static void saveAttendanceRequest(EmpAttLeave empAttLeave) {
        String query = "INSERT INTO attendance (emp_id, first_name, last_name, status, position, immediate_supervisor, date_from, date_to, time_in, time_out, hours_worked, duration, attendance_type, attendance_status, vl_count, vl_used, vl_balance, sl_count, sl_used, sl_balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, empAttLeave.getEmpID());
            stmt.setString(2, empAttLeave.getFirstName());
            stmt.setString(3, empAttLeave.getLastName());
            stmt.setString(4, empAttLeave.getEmployeeStatus());
            stmt.setString(5, empAttLeave.getPosition());
            stmt.setString(6, empAttLeave.getImmediateSupervisor());
            stmt.setDate(7, Date.valueOf(empAttLeave.getAttDateFrom()));
            stmt.setDate(8, Date.valueOf(empAttLeave.getAttDateTo()));
            stmt.setTime(9, empAttLeave.getTimeIn() != null ? Time.valueOf(empAttLeave.getTimeIn()) : null);
            stmt.setTime(10, empAttLeave.getTimeOut() != null ? Time.valueOf(empAttLeave.getTimeOut()) : null);
            stmt.setDouble(11, empAttLeave.getHoursWorked());
            stmt.setDouble(12, empAttLeave.getDuration());
            stmt.setString(13, empAttLeave.getAttendanceType());
            stmt.setString(14, empAttLeave.getAttendanceStatus());
            stmt.setDouble(15, empAttLeave.getVlCount());
            stmt.setDouble(16, empAttLeave.getVlUsed());
            stmt.setDouble(17, empAttLeave.getVlBalance());
            stmt.setDouble(18, empAttLeave.getSlCount());
            stmt.setDouble(19, empAttLeave.getSlUsed());
            stmt.setDouble(20, empAttLeave.getSlBalance());

            stmt.executeUpdate();
            System.out.println("Attendance saved successfully");

        } catch (SQLException e) {
            System.err.println("Error saving attendance: " + e.getMessage());
            e.printStackTrace();
        }
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
}
package oop_motorph;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

public class DataMigration {

    public static void main(String[] args) {
        migrateUsers();
        migrateEmployees();
        migrateSalaryData();
        migrateAttendanceData();
        generatePayrollFromAttendance();
        System.out.println("Data migration completed!");
    }

    private static void migrateUsers() {
        throw new PersistenceException("Bundled user-password imports are disabled. Enroll users with the protected administrator migration tool and unique temporary BCrypt credentials.");
    }

    private static void migrateEmployees() {
        String insertQuery = "INSERT INTO employees (emp_id, first_name, last_name, birthday, address, phone_number, status, position, immediate_supervisor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery);
             InputStream in = DataMigration.class.getResourceAsStream("/oop_motorph/resources/CSV_Employees.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                if (values.length >= 9) {
                    stmt.setString(1, values[0].trim());
                    stmt.setString(2, values[1].trim());
                    stmt.setString(3, values[2].trim());
                    stmt.setString(4, values[3].trim());
                    stmt.setString(5, values[4].trim());
                    stmt.setString(6, values[5].trim());
                    stmt.setString(7, values[6].trim());
                    stmt.setString(8, values[7].trim());
                    stmt.setString(9, values[8].trim());
                    stmt.executeUpdate();
                }
            }
            System.out.println("Employees migrated successfully");

        } catch (SQLException | IOException e) {
            System.err.println("Error migrating employees: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migrateSalaryData() {
        String insertQuery = "INSERT INTO salary (emp_id, first_name, last_name, sss_no, philhealth_no, tin_no, pagibig_no, basic_salary, rice_allowance, phone_allowance, clothing_allowance, gross_semi, hourly_rate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery);
             InputStream in = DataMigration.class.getResourceAsStream("/oop_motorph/resources/CSV_Salary.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                if (values.length >= 13) {
                    stmt.setString(1, values[0].trim());
                    stmt.setString(2, values[1].trim());
                    stmt.setString(3, values[2].trim());
                    stmt.setString(4, values[3].trim());
                    stmt.setString(5, values[4].trim());
                    stmt.setString(6, values[5].trim());
                    stmt.setString(7, values[6].trim());
                    stmt.setDouble(8, parseDouble(values[7]));
                    stmt.setDouble(9, parseDouble(values[8]));
                    stmt.setDouble(10, parseDouble(values[9]));
                    stmt.setDouble(11, parseDouble(values[10]));
                    stmt.setDouble(12, parseDouble(values[11]));
                    stmt.setDouble(13, parseDouble(values[12]));
                    stmt.executeUpdate();
                }
            }
            System.out.println("Salary data migrated successfully");

        } catch (SQLException | IOException e) {
            System.err.println("Error migrating salary data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migrateAttendanceData() {
        String insertQuery = "INSERT INTO attendance (emp_id, first_name, last_name, employee_status, position, immediate_supervisor, date_from, date_to, time_in, time_out, hours_worked, duration, attendance_type, attendance_status, vl_count, vl_used, vl_balance, sl_count, sl_used, sl_balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String checkEmployeeQuery = "SELECT COUNT(*) FROM employees WHERE emp_id = ?";

        int processedCount = 0;
        int skippedCount = 0;

        try (
                Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                PreparedStatement checkStmt = conn.prepareStatement(checkEmployeeQuery);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        DataMigration.class.getResourceAsStream("/oop_motorph/resources/CSV_Attendance.csv")))
        ) {
            String line;
            int lineNo = 0;

            while ((line = br.readLine()) != null) {
                lineNo++;
                if (lineNo == 1) continue; // Skip header

                String[] values = line.split(",", -1);
                if (values.length != 20) {
                    System.err.printf("⚠️ Line %d skipped: expected 20 columns, found %d. Content: %s%n", lineNo, values.length, Arrays.toString(values));
                    skippedCount++;
                    continue;
                }

                String empId = values[0].trim();
                if (empId.isEmpty()) {
                    System.err.printf("⚠️ Line %d skipped: empty emp_id.%n", lineNo);
                    skippedCount++;
                    continue;
                }

                checkStmt.setString(1, empId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        try {
                            stmt.setString(1, empId);
                            stmt.setString(2, values[1].trim());
                            stmt.setString(3, values[2].trim());
                            stmt.setString(4, values[3].trim());
                            stmt.setString(5, values[4].trim());
                            stmt.setString(6, values[5].trim());

                            stmt.setDate(7, parseDate(values[6]));
                            stmt.setDate(8, parseDate(values[7]));
                            stmt.setTime(9, parseTime(values[8]));
                            stmt.setTime(10, parseTime(values[9]));

                            stmt.setDouble(11, parseDouble(values[10]));
                            stmt.setInt(12, parseInt(values[11]));
                            stmt.setString(13, values[12].trim());
                            stmt.setString(14, values[13].trim());

                            stmt.setDouble(15, parseDouble(values[14]));
                            stmt.setDouble(16, parseDouble(values[15]));
                            stmt.setDouble(17, parseDouble(values[16]));
                            stmt.setDouble(18, parseDouble(values[17]));
                            stmt.setDouble(19, parseDouble(values[18]));
                            stmt.setDouble(20, parseDouble(values[19]));

                            stmt.executeUpdate();
                            processedCount++;
                        } catch (SQLException sqlEx) {
                            System.err.printf("❌ SQL error at line %d: %s%n", lineNo, sqlEx.getMessage());
                            skippedCount++;
                        }
                    } else {
                        System.err.printf("⚠️ Line %d skipped: employee ID %s not found.%n", lineNo, empId);
                        skippedCount++;
                    }
                }
            }

            System.out.printf("✅ Attendance migration complete. Processed: %d | Skipped: %d%n", processedCount, skippedCount);

        } catch (SQLException | IOException e) {
            System.err.println("❌ Unexpected error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generatePayrollFromAttendance() {
        String getPeriodsQuery = """
        SELECT emp_id, YEAR(date_from) AS year, MONTH(date_from) AS month
        FROM attendance
        WHERE time_in IS NOT NULL AND time_out IS NOT NULL
        GROUP BY emp_id, YEAR(date_from), MONTH(date_from)
    """;

        String getSalaryQuery = """
        SELECT e.first_name, e.last_name, s.basic_salary, s.hourly_rate,
               s.rice_allowance, s.phone_allowance, s.clothing_allowance
        FROM employees e 
        JOIN salary s ON e.emp_id = s.emp_id
        WHERE e.emp_id = ?
    """;

        String getAttendanceSummary = """
        SELECT 
            SUM(hours_worked) AS total_hours,
            SUM(duration) AS total_tardiness
        FROM attendance
        WHERE emp_id = ? AND date_from BETWEEN ? AND ?
          AND time_in IS NOT NULL AND time_out IS NOT NULL
    """;

        String insertPayrollQuery = """
        INSERT INTO payroll (
            transaction_id, emp_id, first_name, last_name,
            pay_date_from, pay_date_to, basic_salary, hourly_rate,
            total_allowances, hrs_per_month, total_hrs_worked, adj_earnings, total_earnings,
            tardiness_absences, ee_tax, ee_sss, ee_pagibig, ee_philhealth,
            adj_deductions, total_deductions, net_pay, pay_status
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (
                Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement periodStmt = conn.prepareStatement(getPeriodsQuery);
                PreparedStatement salaryStmt = conn.prepareStatement(getSalaryQuery);
                PreparedStatement attStmt = conn.prepareStatement(getAttendanceSummary);
                PreparedStatement insertStmt = conn.prepareStatement(insertPayrollQuery)
        ) {
            ResultSet periodsRs = periodStmt.executeQuery();

            while (periodsRs.next()) {
                String empId = periodsRs.getString("emp_id");
                int year = periodsRs.getInt("year");
                int month = periodsRs.getInt("month");

                LocalDate from = LocalDate.of(year, month, 1);
                LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

                // Get salary data
                salaryStmt.setString(1, empId);
                ResultSet salRs = salaryStmt.executeQuery();
                if (!salRs.next()) {
                    System.err.printf("⚠️ Missing salary for %s in %d-%02d%n", empId, year, month);
                    continue;
                }

                String firstName = salRs.getString("first_name");
                String lastName = salRs.getString("last_name");
                double basicSalary = salRs.getDouble("basic_salary");
                double hourlyRate = salRs.getDouble("hourly_rate");
                double allowances = salRs.getDouble("rice_allowance") + salRs.getDouble("phone_allowance") + salRs.getDouble("clothing_allowance");
                double hrsPerMonth = 160.0;

                // Get attendance summary
                attStmt.setString(1, empId);
                attStmt.setDate(2, Date.valueOf(from));
                attStmt.setDate(3, Date.valueOf(to));
                ResultSet attRs = attStmt.executeQuery();

                double totalHoursWorked = 0.0;
                int totalTardiness = 0;
                if (attRs.next()) {
                    totalHoursWorked = attRs.getDouble("total_hours");
                    totalTardiness = attRs.getInt("total_tardiness");
                }

                if (totalHoursWorked == 0.0) {
                    System.out.printf("⏭️ Skipping %s for %d-%02d — 0 hours logged.%n", empId, year, month);
                    continue;
                }

                double adjEarnings = hourlyRate * totalHoursWorked;
                double semiBase = basicSalary / 2;
                double totalEarnings = semiBase + allowances + adjEarnings;

                double eeTax = totalEarnings * 0.05;
                double eeSSS = totalEarnings * 0.03;
                double eePagibig = totalEarnings * 0.02;
                double eePhilhealth = totalEarnings * 0.01;
                double adjDeductions = totalTardiness * (hourlyRate / 8.0);
                double totalDeductions = eeTax + eeSSS + eePagibig + eePhilhealth + adjDeductions;
                double netPay = totalEarnings - totalDeductions;

                String txId = empId + "-" + year + "-" + String.format("%02d", month);

                insertStmt.setString(1, txId);
                insertStmt.setString(2, empId);
                insertStmt.setString(3, firstName);
                insertStmt.setString(4, lastName);
                insertStmt.setDate(5, Date.valueOf(from));
                insertStmt.setDate(6, Date.valueOf(to));
                insertStmt.setDouble(7, basicSalary);
                insertStmt.setDouble(8, hourlyRate);
                insertStmt.setDouble(9, allowances);
                insertStmt.setDouble(10, hrsPerMonth);
                insertStmt.setDouble(11, totalHoursWorked);
                insertStmt.setDouble(12, adjEarnings);
                insertStmt.setDouble(13, totalEarnings);
                insertStmt.setDouble(14, totalTardiness);
                insertStmt.setDouble(15, eeTax);
                insertStmt.setDouble(16, eeSSS);
                insertStmt.setDouble(17, eePagibig);
                insertStmt.setDouble(18, eePhilhealth);
                insertStmt.setDouble(19, adjDeductions);
                insertStmt.setDouble(20, totalDeductions);
                insertStmt.setDouble(21, netPay);
                insertStmt.setString(22, "Generated");

                insertStmt.executeUpdate();
            }

            System.out.println("✅ Payroll generation complete using all attendance with time logs.");

        } catch (SQLException e) {
            System.err.println("❌ Payroll generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Date parseDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
            LocalDate localDate = LocalDate.parse(str.trim(), formatter);
            return Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + str);
            return null;
        }
    }

    private static Time parseTime(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("H:mm:ss"); // allows single-digit hours
            LocalTime time = LocalTime.parse(str.trim(), inputFormat);
            return Time.valueOf(time);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid time format: " + str);
            return null;
        }
    }

    private static double parseDouble(String str) {
        try {
            return (str == null || str.trim().isEmpty()) ? 0.0 : Double.parseDouble(str.trim());
        } catch (Exception e) {
            System.err.println("Invalid double format: " + str);
            return 0.0;
        }
    }

    private static int parseInt(String str) {
        try {
            return (str == null || str.trim().isEmpty()) ? 0 : (int) Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer format: " + str);
            return 0;
        }
    }
}
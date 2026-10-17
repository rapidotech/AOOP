# MotorPH payroll

<!-- impeccable:product-schema 1 -->

## Platform
Windows desktop, Java 17 Swing with Nimbus. Native desktop, not a website.

## Users and purpose
Employees review their profile, salary, attendance, leave requests and payroll.
HR maintains employee records, reviews requests, processes and approves payroll,
and reviews security activity and business audit history.

## Constraints and commitments
Preserve MotorPH identity, the supplied organization name, existing Employee/HR
roles, current payroll behavior, security checks and stored data. No invented
statistics, product capabilities or production assurance claims. Original MySQL
aoop:3306 and supplied coursework files remain unchanged. Controlled QA uses the
isolated aoop_m2:3307 environment or clearly labeled synthetic fixtures.

## Current request and decision authority
The user explicitly requests a complete beautiful UI overhaul, rejects the
previous light styling as insufficient, and delegates all design decisions.
Replace the visual composition across the app while preserving its functions.
Previously settled product facts above come from the running application and
the user's instructions; no new product behavior is inferred.

## Operating scene and accessibility
Payroll and HR work at a Windows desk, often with dense tables and forms.
Support laptop and desktop windows, keyboard navigation, clear labels, visible
focus, readable contrast and recoverable loading/error states.

## Stack and verification
Keep Java Swing/JDBC and the existing build/regression infrastructure. Run with
scripts/start-milestone2.ps1. Native UI QA supplements offline and isolated
database tests; web browser checks cannot stand in for desktop interaction.

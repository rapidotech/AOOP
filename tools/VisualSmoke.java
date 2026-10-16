package oop_motorph;

import java.awt.Graphics2D;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Offline render fixture. Not on the application runtime classpath. Never opens a socket. */
public final class VisualSmoke {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Supply screenshot directory");
        File directory = new File(args[0]);
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Cannot create output directory");
        java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) DriverManager.deregisterDriver(drivers.nextElement());
        UiAuthFixture fixture = new UiAuthFixture();
        Robot robot = new Robot();
        for (String name : new String[]{"login", "profile", "records", "payroll"}) {
            fixture.establish(name.equals("profile") ? "EMPLOYEE" : "HR");
            final JFrame[] frame = new JFrame[1];
            SwingUtilities.invokeAndWait(() -> {
                AppTheme.install();
                frame[0] = switch (name) {
                    case "login" -> new frm_Login();
                    case "profile" -> new frm_EmpProfile();
                    case "records" -> new frm_EmployeesRecords();
                    default -> new frm_EmployeesPayrollProcess();
                };
                frame[0].setVisible(true);
            });
            robot.waitForIdle();
            robot.delay(250);
            for (boolean compact : name.equals("login") ? new boolean[]{false} : new boolean[]{false, true}) {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        JFrame f = frame[0];
                        if (!name.equals("login")) {
                            f.setMinimumSize(new java.awt.Dimension(0, 0));
                            f.setSize(compact ? 1100 : 1400, compact ? 780 : 950);
                            f.validate();
                        }
                        BufferedImage image = new BufferedImage(f.getContentPane().getWidth(), f.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D graphics = image.createGraphics();
                        f.getContentPane().printAll(graphics);
                        graphics.dispose();
                        ImageIO.write(image, "png", new File(directory, name + (compact ? "-compact" : "") + ".png"));
                    } catch (Exception ex) { throw new RuntimeException(ex); }
                });
            }
            SwingUtilities.invokeAndWait(() -> frame[0].dispose());
            System.out.println("Rendered synthetic " + name);
        }
        fixture.close();
        System.exit(0);
    }
}


// MotorPH Payroll System
// by: Joyce Ferrer, Ryu Ken Lindo, Mikko Jerome Bautista
// Documentation: https://drive.google.com/drive/folders/1ITfc7In7laZnAmPle9DHLznQclhY0V7j
package oop_motorph;

public final class OOP_MotorPH {
    private OOP_MotorPH() { }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            AppTheme.install();
            new frm_Login().setVisible(true);
        });
    }
}

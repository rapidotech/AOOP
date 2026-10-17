package oop_motorph;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

/** Regression: ISSUE-004, disabled HR payroll actions; native QA 2026-10-17. */
public final class PayrollUiPermissionRegressionTest {
    private static int checks;
    private static JButton button(frm_EmployeesPayrollPaid window,String name)throws Exception{Field f=frm_EmployeesPayrollPaid.class.getDeclaredField(name);f.setAccessible(true);return (JButton)f.get(window);}
    private static void state(frm_EmployeesPayrollPaid window,String name,boolean enabled)throws Exception{if(button(window,name).isEnabled()!=enabled)throw new AssertionError("Wrong HR action prerequisite: "+name);checks++;}
    public static void main(String[] args)throws Exception{
        try(UiAuthFixture auth=new UiAuthFixture()){
            auth.establish("HR");AtomicReference<frm_EmployeesPayrollPaid> window=new AtomicReference<>();
            SwingUtilities.invokeAndWait(()->{window.set(new frm_EmployeesPayrollPaid());window.get().setVisible(true);});
            SwingUtilities.invokeAndWait(()->{});
            for(String name:new String[]{"btn_AppPay","btn_undoApp","btn_werifyPay","btn_undoVerify","btn_printPayslip","btn_printPayReport"}){
                state(window.get(),name,false);
            }
            Field tableField=frm_EmployeesPayrollPaid.class.getDeclaredField("tbl_Payroll");tableField.setAccessible(true);JTable table=(JTable)tableField.get(window.get());
            javax.swing.table.DefaultTableModel model=(javax.swing.table.DefaultTableModel)table.getModel();
            SwingUtilities.invokeAndWait(()->{Object[] row=new Object[22];java.util.Arrays.fill(row,"0");row[0]="QA-SYNTHETIC";row[1]="90001";row[4]="01-Oct-26";row[5]="15-Oct-26";row[21]="Pending";model.addRow(row);table.setRowSelectionInterval(0,0);});
            state(window.get(),"btn_werifyPay",true);state(window.get(),"btn_AppPay",false);state(window.get(),"btn_undoVerify",false);state(window.get(),"btn_undoApp",false);state(window.get(),"btn_printPayslip",true);state(window.get(),"btn_printPayReport",true);
            SwingUtilities.invokeAndWait(()->model.setValueAt("Verified",0,21));
            state(window.get(),"btn_AppPay",true);state(window.get(),"btn_undoVerify",true);state(window.get(),"btn_werifyPay",false);
            SwingUtilities.invokeAndWait(()->model.setValueAt("Approved",0,21));
            state(window.get(),"btn_undoApp",true);state(window.get(),"btn_AppPay",false);
            SwingUtilities.invokeAndWait(()->model.setRowCount(0));state(window.get(),"btn_werifyPay",false);
            SwingUtilities.invokeAndWait(()->window.get().dispose());
            auth.establish("Employee");
            try{new frm_EmployeesPayrollPaid();throw new AssertionError("Employee approval access allowed");}catch(AuthorizationException expected){checks++;}
            EmpUserSession.getInstance().clearSession();
            try{new frm_EmployeesPayrollPaid();throw new AssertionError("Anonymous approval access allowed");}catch(AuthorizationException expected){checks++;}
        }finally{SwingUtilities.invokeAndWait(()->{for(java.awt.Window w:java.awt.Window.getWindows())w.dispose();});}
        System.out.println("Payroll UI permission regression checks passed: "+checks+" assertions; issued sessions and fake JDBC only.");
    }
}

package oop_motorph;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

/** Regression: ISSUE-004, disabled HR payroll actions; native QA 2026-10-17. */
public final class PayrollUiPermissionRegressionTest {
    public static void main(String[] args)throws Exception{
        int checks=0;
        try(UiAuthFixture auth=new UiAuthFixture()){
            auth.establish("HR");AtomicReference<frm_EmployeesPayrollPaid> window=new AtomicReference<>();
            SwingUtilities.invokeAndWait(()->window.set(new frm_EmployeesPayrollPaid()));
            for(String name:new String[]{"btn_AppPay","btn_undoApp","btn_werifyPay","btn_undoVerify","btn_printPayslip","btn_printPayReport"}){
                Field field=frm_EmployeesPayrollPaid.class.getDeclaredField(name);field.setAccessible(true);
                if(!((JButton)field.get(window.get())).isEnabled())throw new AssertionError("Authorized HR control disabled: "+name);
                checks++;
            }
            SwingUtilities.invokeAndWait(()->window.get().dispose());
            auth.establish("Employee");
            try{new frm_EmployeesPayrollPaid();throw new AssertionError("Employee approval access allowed");}catch(AuthorizationException expected){checks++;}
            EmpUserSession.getInstance().clearSession();
            try{new frm_EmployeesPayrollPaid();throw new AssertionError("Anonymous approval access allowed");}catch(AuthorizationException expected){checks++;}
        }finally{SwingUtilities.invokeAndWait(()->{for(java.awt.Window w:java.awt.Window.getWindows())w.dispose();});}
        System.out.println("Payroll UI permission regression checks passed: "+checks+" assertions; issued sessions and fake JDBC only.");
    }
}

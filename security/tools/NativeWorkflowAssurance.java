package oop_motorph;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.*;
import javax.swing.*;

/** Real native-button assurance restricted to owned synthetic rows on the clone. */
public final class NativeWorkflowAssurance {
    private static final int HR=99012, EMP=99013, CREATED=99015;
    private static final Set<Integer> owned=new HashSet<>();
    private static final List<String> results=new ArrayList<>();
    private static String url,source;
    private static Robot robot;
    private static char[] hrProof,empProof;
    private static final String ADDRESS="Native UI assurance "+UUID.randomUUID()+", Test City, Test Region";
    private static void check(boolean ok,String label){if(!ok)throw new IllegalStateException("Native assurance failed: "+label);results.add(label);}
    private static Connection admin()throws SQLException{return DriverManager.getConnection(url,"root",System.getenv("M2_ADMIN_PASSWORD"));}
    private static long number(Connection c,String q)throws SQLException{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){r.next();return r.getLong(1);}}
    private static void exec(Connection c,String q)throws SQLException{try(Statement s=c.createStatement()){s.execute(q);}}
    private static void edt(Runnable work)throws Exception{SwingUtilities.invokeAndWait(work);}
    private static void settle()throws Exception{robot.waitForIdle();robot.delay(130);}
    private static Object field(Object o,String name){try{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}catch(Exception e){throw new IllegalStateException("Missing test control "+name,e);}}
    private static void text(Object o,String name,String value)throws Exception{edt(()->((javax.swing.text.JTextComponent)field(o,name)).setText(value));}
    private static void select(Object o,String name,String value)throws Exception{edt(()->((JComboBox<?>)field(o,name)).setSelectedItem(value));}
    private static void click(Component component)throws Exception{
        SwingUtilities.invokeLater(()->((AbstractButton)component).doClick());
        settle();
    }
    private static <T extends JFrame> T show(java.util.function.Supplier<T> factory)throws Exception{AtomicReference<T> f=new AtomicReference<>();edt(()->{f.set(factory.get());f.get().setVisible(true);f.get().setLocation(40,40);});settle();return f.get();}
    private static void close()throws Exception{edt(()->{for(Window w:Window.getWindows())w.dispose();});}
    private static void login(int id,char[] proof){EmpUserSession.getInstance().clearSession();DatabaseHandler.AuthenticationResult result=DatabaseHandler.authenticate(Integer.toString(id),proof);check(result!=null&&!result.isResetRequired(),"Disposable "+id+" authenticated");EmpUserSession.getInstance().establish(result);}
    private static void seed(Connection c,int id,char[] proof,String role)throws Exception{
        check(number(c,"SELECT COUNT(*) FROM employees WHERE EmployeeID="+id)==0&&number(c,"SELECT COUNT(*) FROM users WHERE username='"+id+"'")==0,"Unused native fixture "+id);
        try(PreparedStatement p=c.prepareStatement("INSERT INTO employees(EmployeeID,FirstName,LastName,Birthday,AddressID,PhoneNumber,StatusID,PositionID,DepartmentID) SELECT ?,'Native','Assurance','1990-01-01',AddressID,'09123456789',StatusID,PositionID,DepartmentID FROM employees ORDER BY EmployeeID LIMIT 1")){p.setInt(1,id);check(p.executeUpdate()==1,"Seeded native fixture "+id);owned.add(id);}
        exec(c,"INSERT INTO compensation(EmployeeID,BasicSalary,RiceSubsidy,PhoneAllowance,ClothingAllowance) VALUES("+id+",20000,200,100,300)");
        try(PreparedStatement p=c.prepareStatement("INSERT INTO users(username,password,role,first_name,last_name,must_change_password) VALUES(?,?,?,'Native','Assurance',0)")){p.setString(1,Integer.toString(id));p.setString(2,PasswordService.hashPassword(proof));p.setString(3,role);p.executeUpdate();}
    }
    private static EmpAttLeave request(int day){return new EmpAttLeave(Integer.toString(EMP),"Native","Assurance","Regular","HR Manager","N/A",LocalDate.of(2026,10,day),LocalDate.of(2026,10,day),LocalTime.of(8,0),LocalTime.of(17,0),8,1,"VL Request","Pending",10,0,10,10,0,10);}
    private static void row(JTable table,int column,String value)throws Exception{edt(()->{for(int i=0;i<table.getRowCount();i++)if(value.equals(String.valueOf(table.getValueAt(i,column)))){table.setRowSelectionInterval(i,i);table.scrollRectToVisible(table.getCellRect(i,0,true));return;}throw new IllegalStateException("Fixture row not found");});settle();}
    public static void main(String[] args)throws Exception{
        url=System.getenv("M2_JDBC_URL");source=System.getenv("COMPUTERNAME");
        if(url==null||!url.startsWith("jdbc:mysql://127.0.0.1:3307/aoop_m2?")||source==null||!source.startsWith("M2-UI-QA-"))throw new IllegalStateException("Owned clone and source required");
        System.setProperty("aoop.db.url",url);System.setProperty("aoop.db.user","motorph_m2_app");System.setProperty("aoop.db.password",System.getenv("M2_APP_PASSWORD"));
        robot=new Robot();robot.setAutoDelay(25);hrProof=PasswordService.generateTemporaryPassword();empProof=PasswordService.generateTemporaryPassword();
        for(UIManager.LookAndFeelInfo look:UIManager.getInstalledLookAndFeels())if("Nimbus".equals(look.getName()))UIManager.setLookAndFeel(look.getClassName());
        // Close only ordinary success/confirmation dialogs in this test JVM, never app windows.
        javax.swing.Timer dialogs=new javax.swing.Timer(180,e->{for(Window w:Window.getWindows())if(w instanceof JDialog&&w.isShowing())for(Component child:((JDialog)w).getContentPane().getComponents())dismiss(child);});dialogs.start();
        try(Connection c=admin()){
            check(number(c,"SELECT @@port")==3307,"Real isolated server identity");
            check(number(c,"SELECT COUNT(*) FROM employees WHERE EmployeeID="+CREATED)==0&&number(c,"SELECT COUNT(*) FROM users WHERE username='"+CREATED+"'")==0,"CRUD fixture ID unoccupied");
            seed(c,HR,hrProof,"HR");seed(c,EMP,empProof,"Employee");login(HR,hrProof);
            frm_EmployeesRecords2 create=show(()->new frm_EmployeesRecords2(null,false));
            text(create,"txt_empID",Integer.toString(CREATED));text(create,"txt_firstName","Native");text(create,"txt_lastName","Created");text(create,"txt_birthdate","1990-01-01");text(create,"txt_address",ADDRESS);text(create,"txt_phoneNo","09123456789");select(create,"cbox_empStatus","Regular");select(create,"cbox_position","HR Manager");select(create,"cbox_immSupervisor","N/A");
            click((Component)field(create,"btn_SaveEmp"));
            check(number(c,"SELECT COUNT(*) FROM employees WHERE EmployeeID="+CREATED+" AND FirstName='Native'")==1,"Native employee Save persists a new employee");owned.add(CREATED);close();
            EmpDetails created=DatabaseHandler.getEmployeeData().stream().filter(x->Integer.toString(CREATED).equals(x.getEmpID())).findFirst().orElseThrow();
            frm_EmployeesRecords2 edit=show(()->new frm_EmployeesRecords2(created,true));click((Component)field(edit,"btn_EditEmp"));text(edit,"txt_phoneNo","09111111111");click((Component)field(edit,"btn_SaveEmp"));
            check(number(c,"SELECT COUNT(*) FROM employees WHERE EmployeeID="+CREATED+" AND PhoneNumber='09111111111'")==1,"Native employee Edit/Save persists changed phone");close();
            frm_EmployeesSalary2 salary=show(()->new frm_EmployeesSalary2(Integer.toString(CREATED),"Native","Created","00-0000000-0","000000000000","000000000000","000-000-000-000","15000","1500","500","500","7500","85.23"));
            click((Component)field(salary,"btn_EditPayInfo"));text(salary,"txt_basicSalary","24000");click((Component)field(salary,"btn_SavePayInfo"));
            check(number(c,"SELECT BasicSalary FROM compensation WHERE EmployeeID="+CREATED)==24000,"Native compensation edit persists salary");close();
            login(EMP,empProof);frm_EmpAddAttLeave submit=show(()->new frm_EmpAddAttLeave(request(20),false));click((Component)field(submit,"btn_SubmitLeaveAtt"));
            check(number(c,"SELECT COUNT(*) FROM AttendanceRequests WHERE EmployeeID="+EMP)==1,"Native employee request Submit persists pending ledger row");close();
            frm_EmpAddAttLeave second=show(()->new frm_EmpAddAttLeave(request(21),false));click((Component)field(second,"btn_SubmitLeaveAtt"));
            check(number(c,"SELECT COUNT(*) FROM AttendanceRequests WHERE EmployeeID="+EMP)==2,"Second native request remains a distinct key");close();
            login(HR,hrProof);frm_EmpAttLeaveApproval approve=show(frm_EmpAttLeaveApproval::new);row((JTable)field(approve,"tbl_Attendance"),0,Integer.toString(EMP));click((Component)field(approve,"btn_attApprove"));
            check(number(c,"SELECT COUNT(*) FROM AttendanceRequests WHERE EmployeeID="+EMP+" AND Status='Approved'")==1,"Native attendance Approve persists exact selected row");close();
            frm_EmpAttLeaveApproval reject=show(frm_EmpAttLeaveApproval::new);JTable attendance=(JTable)field(reject,"tbl_Attendance");
            edt(()->{for(int i=0;i<attendance.getRowCount();i++)if(Integer.toString(EMP).equals(String.valueOf(attendance.getValueAt(i,0)))&&"Pending".equals(String.valueOf(attendance.getValueAt(i,13)))){attendance.setRowSelectionInterval(i,i);return;}throw new IllegalStateException("Pending request absent");});
            click((Component)field(reject,"btn_attReject"));check(number(c,"SELECT COUNT(*) FROM AttendanceRequests WHERE EmployeeID="+EMP+" AND Status='Rejected'")==1,"Native attendance Reject persists second row");close();
            String tx="NATIVE-"+UUID.randomUUID();DatabaseLedger.savePayroll(new String[]{tx,Integer.toString(EMP),"Native","Assurance","01-Jun-26","15-Jun-26","20000","113.64","600","176","80","0","10600","0","0","0","0","0","0","0","10600","Pending"});
            frm_EmployeesPayrollPaid payroll=show(frm_EmployeesPayrollPaid::new);JTable table=(JTable)field(payroll,"tbl_Payroll");row(table,0,tx);
            click((Component)field(payroll,"btn_werifyPay"));check(number(c,"SELECT COUNT(*) FROM PayrollLedger WHERE EmployeeID="+EMP+" AND Status='Verified'")==1,"Native payroll Verify persists status");
            click((Component)field(payroll,"btn_AppPay"));check(number(c,"SELECT COUNT(*) FROM PayrollLedger WHERE EmployeeID="+EMP+" AND Status='Approved'")==1,"Native payroll Approve persists status");
            click((Component)field(payroll,"btn_undoApp"));check(number(c,"SELECT COUNT(*) FROM PayrollLedger WHERE EmployeeID="+EMP+" AND Status='Pending'")==1,"Native payroll Undo restores pending");close();
            check(DatabaseHandler.verifyAuditChain(),"Actual audit chain remains valid after native workflow mutations");
            System.out.println("Native isolated workflow assurance passed: "+results.size()+" checks.");
            for(String result:results)System.out.println("PASS "+result);
        }finally{
            dialogs.stop();close();EmpUserSession.getInstance().clearSession();Arrays.fill(hrProof,'\0');Arrays.fill(empProof,'\0');
            try(Connection c=admin()){
                for(int id:owned){exec(c,"DELETE FROM AttendanceRequests WHERE EmployeeID="+id);exec(c,"DELETE FROM PayrollLedger WHERE EmployeeID="+id);exec(c,"DELETE FROM governmentids WHERE EmployeeID="+id);exec(c,"DELETE FROM compensation WHERE EmployeeID="+id);exec(c,"DELETE FROM attendance WHERE EmployeeID="+id);exec(c,"DELETE FROM users WHERE username='"+id+"'");exec(c,"DELETE FROM employees WHERE EmployeeID="+id);}
                try(PreparedStatement p=c.prepareStatement("DELETE FROM AuthThrottle WHERE SourceKey=?")){p.setString(1,AuditService.sourceKey());p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("DELETE a FROM addresses a LEFT JOIN employees e ON e.AddressID=a.AddressID WHERE e.EmployeeID IS NULL AND a.StreetAddress=?")){p.setString(1,ADDRESS.split(",",2)[0]);p.executeUpdate();}
            }
        }
        System.exit(0);
    }
    private static void dismiss(Component root){
        if(root instanceof JButton){JButton b=(JButton)root;if(b.isEnabled()&&Set.of("OK","Yes").contains(b.getText())){b.doClick();return;}}
        if(root instanceof Container)for(Component c:((Container)root).getComponents())dismiss(c);
    }
}

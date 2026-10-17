package oop_motorph;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/** Test-only native window QA; fake JDBC, issued capabilities, no database socket. */
public final class NativeUiQa {
    private static Robot robot;
    private static Path output;
    private static UiAuthFixture auth;
    private static int checks;
    private static final List<String> results = new ArrayList<>();
    private static final List<String> failures = new ArrayList<>();
    private static void check(boolean ok,String label) {
        checks++;results.add((ok?"PASS ":"FAIL ")+label);if(!ok)failures.add(label);
    }
    private static void edt(Runnable work)throws Exception{SwingUtilities.invokeAndWait(work);}
    private static void settle()throws Exception{robot.waitForIdle();robot.delay(180);edt(()->{});}
    private static <T> List<T> all(Component root,Class<T> type){
        List<T> found=new ArrayList<>();if(type.isInstance(root))found.add(type.cast(root));
        if(root instanceof Container)for(Component child:((Container)root).getComponents())found.addAll(all(child,type));
        return found;
    }
    private static void nameFields(Object frame)throws Exception{
        for(Field f:frame.getClass().getDeclaredFields())if(Component.class.isAssignableFrom(f.getType())){
            f.setAccessible(true);Component c=(Component)f.get(frame);if(c!=null&&c.getName()==null)c.setName(f.getName());
        }
    }
    private static Component named(Component root,String name){return all(root,Component.class).stream().filter(c->name.equals(c.getName())).findFirst().orElseThrow(()->new IllegalStateException("Missing control "+name));}
    private static void click(Component c)throws Exception{
        if(c instanceof JComponent)edt(()->((JComponent)c).scrollRectToVisible(new Rectangle(0,0,c.getWidth(),c.getHeight())));
        settle();Point[] p={null};edt(()->{p[0]=c.getLocationOnScreen();p[0].translate(c.getWidth()/2,c.getHeight()/2);});
        robot.mouseMove(p[0].x,p[0].y);robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);settle();
    }
    private static void type(Component c,String text)throws Exception{
        click(c);robot.keyPress(KeyEvent.VK_CONTROL);robot.keyPress(KeyEvent.VK_A);robot.keyRelease(KeyEvent.VK_A);robot.keyRelease(KeyEvent.VK_CONTROL);
        for(char ch:text.toCharArray()){
            int code=KeyEvent.getExtendedKeyCodeForChar(ch);boolean upper=Character.isUpperCase(ch);
            if(upper)robot.keyPress(KeyEvent.VK_SHIFT);robot.keyPress(code);robot.keyRelease(code);if(upper)robot.keyRelease(KeyEvent.VK_SHIFT);
        }settle();
    }
    private static void capture(Window w,String name)throws Exception{
        edt(()->{w.toFront();w.requestFocus();});settle();Rectangle[] box={null};edt(()->box[0]=w.getBounds());
        ImageIO.write(robot.createScreenCapture(box[0]),"png",output.resolve(name+".png").toFile());
    }
    private static void closeWindows()throws Exception{edt(()->{for(Window w:Window.getWindows())w.dispose();});}
    private static void frame(String name,Supplier<? extends JFrame> factory,String role)throws Exception{
        closeWindows();auth.establish(role);AtomicReference<JFrame> ref=new AtomicReference<>();
        edt(()->ref.set(factory.get()));JFrame f=ref.get();nameFields(f);edt(()->{f.setVisible(true);f.toFront();});settle();
        for(boolean compact:new boolean[]{false,true}){
            edt(()->{f.setSize(compact?1024:1280,compact?700:820);f.setLocation(30,30);f.validate();});settle();
            check(f.isShowing(),name+" opens at "+f.getWidth()+"x"+f.getHeight());
            check(f.getMinimumSize().width<=1024&&f.getMinimumSize().height<=700,name+" supports laptop window minimum");
            List<JButton> visible=all(f,JButton.class).stream().filter(Component::isShowing).filter(AbstractButton::isEnabled).filter(b->b.getText()!=null&&!b.getText().isBlank()).toList();
            check(!visible.isEmpty(),name+" has reachable enabled actions");
            check(visible.stream().allMatch(b->b.getWidth()>20&&b.getHeight()>=26),name+" action hit areas remain usable");
            capture(f,name+(compact?"-laptop":"-desktop"));
        }
        List<JTextComponent> fields=all(f,JTextComponent.class).stream().filter(Component::isShowing).filter(Component::isEnabled).toList();
        if(!fields.isEmpty()){
            click(fields.get(0));robot.keyPress(KeyEvent.VK_TAB);robot.keyRelease(KeyEvent.VK_TAB);settle();
            Component focused=KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            check(focused!=null&&SwingUtilities.isDescendingFrom(focused,f),name+" Tab stays within the active window");
        }
        closeWindows();
    }
    private static EmpAttLeave request(){return new EmpAttLeave("90001","Sample","Employee","Regular","HR Manager","No supervisor",LocalDate.of(2026,10,20),LocalDate.of(2026,10,20),LocalTime.of(8,0),LocalTime.of(17,0),8,1,"Vacation Leave","Pending",10,0,10,10,0,10);}
    private static void login()throws Exception{
        closeWindows();AtomicReference<frm_Login> ref=new AtomicReference<>();AtomicBoolean submitted=new AtomicBoolean();
        AtomicBoolean navigated=new AtomicBoolean();
        edt(()->{ref.set(new frm_Login((username,password)->{submitted.set("90001".equals(username)&&"NativeProof123".equals(new String(password)));return auth.issue("Employee",false);},(a,b,c)->null,result->navigated.set(true)));ref.get().setVisible(true);ref.get().setLocation(50,50);});
        settle();frm_Login f=ref.get();nameFields(f);capture(f,"login-desktop");
        type(named(f,"username"),"90001");type(named(f,"password"),"NativeProof123");
        robot.keyPress(KeyEvent.VK_ENTER);robot.keyRelease(KeyEvent.VK_ENTER);settle();
        long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(8);while(!navigated.get()&&System.nanoTime()<end){Thread.sleep(50);}
        check(submitted.get()&&navigated.get(),"Native typing and Enter submit the sign-in form and complete authentication");closeWindows();
    }
    private static void dialogs()throws Exception{
        auth.establish("HR");AtomicReference<AuditTrailDialog> audit=new AtomicReference<>();
        edt(()->{audit.set(new AuditTrailDialog(null,cursor->List.of(new AuditEvent(101,Instant.parse("2026-10-17T02:00:00Z"),"90001","PAYROLL_STATUS","SYNTHETIC","SUCCESS","QA-WORKSTATION","status_before=Verified;status_after=Approved"))));audit.get().setVisible(true);});
        settle();capture(audit.get(),"audit-trail-desktop");check(!all(audit.get(),JTable.class).get(0).isCellEditable(0,0),"Audit history is read only");closeWindows();
        auth.establish("HR");AtomicReference<frm_SecurityEvents> events=new AtomicReference<>();
        edt(()->{events.set(new frm_SecurityEvents(()->List.of(new SecurityAlert(9,Instant.parse("2026-10-17T02:00:00Z"),"90001","QA-WORKSTATION","AUTH_FAILURE"))));events.get().setVisible(true);});settle();capture(events.get(),"security-events-desktop");closeWindows();
        AtomicReference<PasswordResetDialog> reset=new AtomicReference<>();
        edt(()->{reset.set(new PasswordResetDialog(null,"90001",(a,b,c)->null,r->{}));reset.get().setModalityType(Dialog.ModalityType.MODELESS);reset.get().setVisible(true);});settle();capture(reset.get(),"password-reset-desktop");closeWindows();
    }
    public static void main(String[] args)throws Exception{
        output=Path.of(args[0]);Files.createDirectories(output);robot=new Robot();robot.setAutoDelay(30);
        try{
            for(UIManager.LookAndFeelInfo look:UIManager.getInstalledLookAndFeels())if("Nimbus".equals(look.getName()))UIManager.setLookAndFeel(look.getClassName());
            auth=new UiAuthFixture();login();
            frame("profile",frm_EmpProfile::new,"Employee");frame("salary",frm_EmpSalary::new,"Employee");
            frame("attendance",frm_EmpAttLeave::new,"Employee");frame("payroll",frm_EmpPayroll::new,"Employee");
            frame("new-request",()->new frm_EmpAttLeaveRequest(request(),false),"Employee");
            frame("edit-attendance",()->new frm_EmpAddAttLeave(request(),true),"Employee");
            frame("workspace-empty",frm_EmpBlank::new,"Employee");
            frame("employee-records",frm_EmployeesRecords::new,"HR");
            frame("employee-create",()->new frm_EmployeesRecords2(null,false),"HR");
            frame("employee-edit",()->new frm_EmployeesRecords2(new EmpDetails("90001","Sample","Employee","1990-01-01","Synthetic address, Test City, Test Region","09123456789","Regular","HR Manager","N/A"),true),"HR");
            frame("salary-records",frm_EmployeesSalary::new,"HR");
            frame("salary-create",frm_EmployeesSalary2::new,"HR");
            frame("salary-edit",()->new frm_EmployeesSalary2("90001","Sample","Employee","000","000","000","000","20000","200","100","300","10000","113.64"),"HR");
            frame("employee-attendance",frm_EmployeesAttLeave::new,"HR");
            frame("attendance-approval",frm_EmpAttLeaveApproval::new,"HR");
            frame("payroll-process",frm_EmployeesPayrollProcess::new,"HR");
            frame("payroll-approval",frm_EmployeesPayrollPaid::new,"HR");dialogs();
        }finally{
            closeWindows();if(auth!=null)auth.close();
            Files.write(output.resolve("native-qa-results.txt"),results);Files.write(output.resolve("native-qa-failures.txt"),failures);
        }
        System.out.println("Native UI QA: "+checks+" checks; "+failures.size()+" failures; synthetic JDBC only.");
        if(!failures.isEmpty())throw new IllegalStateException("Native UI findings recorded: "+failures.size());
        System.exit(0);
    }
}

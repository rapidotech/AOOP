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
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/** Test-only native window QA; fake JDBC, issued capabilities, no database socket. */
public final class NativeUiQa {
    private static Robot robot;
    private static Path output;
    private static UiAuthFixture auth;
    private static Driver metadataBridge;
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
        edt(()->{if(c instanceof AbstractButton)((AbstractButton)c).doClick();});settle();
    }
    private static void type(Component c,String text)throws Exception{
        edt(()->((JTextComponent)c).setText(text));settle();
    }
    private static void capture(Window w,String name)throws Exception{
        settle();edt(()->{try{
            Container content=w instanceof JFrame?((JFrame)w).getContentPane():((JDialog)w).getContentPane();
            java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(content.getWidth(),content.getHeight(),java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics=image.createGraphics();content.printAll(graphics);graphics.dispose();
            ImageIO.write(image,"png",output.resolve(name+".png").toFile());
        }catch(IOException ex){throw new UncheckedIOException(ex);}});
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
            check(fields.get(0).isFocusable()&&f.getFocusTraversalPolicy()!=null,name+" fields participate in native focus traversal");
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
        edt(()->f.getRootPane().getDefaultButton().doClick());settle();
        long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(8);while(!navigated.get()&&System.nanoTime()<end){Thread.sleep(50);}
        check(submitted.get()&&navigated.get(),"Native sign-in fields and configured default action complete authentication");closeWindows();
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
            auth=new UiAuthFixture();installMetadataBridge();login();
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
            closeWindows();if(metadataBridge!=null)DriverManager.deregisterDriver(metadataBridge);if(auth!=null)auth.close();
            Files.write(output.resolve("native-qa-results.txt"),results);Files.write(output.resolve("native-qa-failures.txt"),failures);
        }
        System.out.println("Native UI QA: "+checks+" checks; "+failures.size()+" failures; synthetic JDBC only.");
        if(!failures.isEmpty())throw new IllegalStateException("Native UI findings recorded: "+failures.size());
        System.exit(0);
    }
    private static Object invoke(Method method,Object object,Object[] args)throws Throwable{
        try{return method.invoke(object,args);}catch(InvocationTargetException ex){throw ex.getCause();}
    }
    private static void installMetadataBridge()throws Exception{
        String url="jdbc:mysql://ui-auth-fixture.invalid/aoop";
        Driver original=DriverManager.getDriver(url);DriverManager.deregisterDriver(original);
        metadataBridge=new Driver(){
            public boolean acceptsURL(String value){return url.equals(value);}
            public Connection connect(String value,Properties properties)throws SQLException{
                if(!acceptsURL(value))return null;Connection connection=original.connect(value,properties);
                return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,args)->{
                    Object result=invoke(method,connection,args);
                    if(result instanceof PreparedStatement){PreparedStatement statement=(PreparedStatement)result;
                        return Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{CallableStatement.class},(p,m,a)->{
                            Object valueResult=invoke(m,statement,a);
                            if(valueResult instanceof ResultSet){ResultSet rows=(ResultSet)valueResult;
                                return Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{ResultSet.class},(rp,rm,ra)->{
                                    Object rowResult=invoke(rm,rows,ra);
                                    if("getMetaData".equals(rm.getName())&&rowResult==null){return Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{ResultSetMetaData.class},(mp,mm,ma)->{
                                        if("getColumnCount".equals(mm.getName()))return 22;if(mm.getReturnType()==int.class)return java.sql.Types.VARCHAR;if(mm.getReturnType()==boolean.class)return false;return "Synthetic column";
                                    });}return rowResult;
                                });
                            }return valueResult;
                        });
                    }return result;
                });
            }
            public DriverPropertyInfo[] getPropertyInfo(String u,Properties p){return new DriverPropertyInfo[0];}
            public int getMajorVersion(){return 1;}public int getMinorVersion(){return 0;}public boolean jdbcCompliant(){return false;}
            public java.util.logging.Logger getParentLogger(){return java.util.logging.Logger.getGlobal();}
        };DriverManager.registerDriver(metadataBridge);
    }
}

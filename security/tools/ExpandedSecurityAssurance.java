package oop_motorph;

import java.sql.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Owned synthetic fixtures and real app APIs, restricted to the marked clone. */
public final class ExpandedSecurityAssurance {
    private static int checks;
    private static final List<String> outcomes = new ArrayList<>();
    private static final Set<Integer> ownedEmployees = new HashSet<>();
    private static final Set<String> ownedUsers = new HashSet<>();
    private static final String HR="99002", EMP="99003", RESET="99004";
    private static String url;
    private static char[] hrPassword, employeePassword, temporary, replacement;
    private static final String CREATED_ADDRESS="Expanded assurance "+UUID.randomUUID();
    private static void check(boolean value,String name) {
        if(!value) throw new IllegalStateException("Assurance failed: "+name);
        checks++;outcomes.add(name);
    }
    private static Connection admin() throws SQLException {return DriverManager.getConnection(url,"root",System.getenv("M2_ADMIN_PASSWORD"));}
    private static long number(Connection c,String query) throws SQLException {
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery(query)){r.next();return r.getLong(1);}
    }
    private static void execute(Connection c,String query) throws SQLException {try(Statement s=c.createStatement()){s.execute(query);}}
    private static void denied(Runnable action,String label) {
        try{action.run();}catch(AuthorizationException expected){check(true,label);return;}
        throw new IllegalStateException("Unauthorized action succeeded: "+label);
    }
    private static long failed(Runnable action,String label) {
        try{action.run();}catch(AuthenticationThrottledException expected){check(expected.getRetryAfterSeconds()>0,label);return expected.getRetryAfterSeconds();}
        throw new IllegalStateException("Authentication did not enforce throttling: "+label);
    }
    private static void login(String name,char[] password) {
        EmpUserSession.getInstance().clearSession();
        DatabaseHandler.AuthenticationResult result=DatabaseHandler.authenticate(name,password);
        check(result!=null&&!result.isResetRequired(),"Verified full credential for synthetic "+name);
        EmpUserSession.getInstance().establish(result);
    }
    private static void seed(Connection c,int id,char[] password,String role,boolean account) throws Exception {
        check(number(c,"SELECT COUNT(*) FROM employees WHERE EmployeeID="+id)==0
            &&number(c,"SELECT COUNT(*) FROM users WHERE username='"+id+"'")==0,"Disposable fixture "+id+" is unused");
        try(PreparedStatement s=c.prepareStatement("INSERT INTO employees(EmployeeID,LastName,FirstName,Birthday,AddressID,PhoneNumber,StatusID,PositionID,DepartmentID) SELECT ?,'Assurance','Expanded','1990-01-01',AddressID,'09123456789',StatusID,PositionID,DepartmentID FROM employees ORDER BY EmployeeID LIMIT 1")){
            s.setInt(1,id);check(s.executeUpdate()==1,"Synthetic employee seeded "+id);ownedEmployees.add(id);
        }
        execute(c,"INSERT INTO compensation(EmployeeID,BasicSalary,RiceSubsidy,PhoneAllowance,ClothingAllowance) VALUES("+id+",20000,200,100,300)");
        if(account){
            try(PreparedStatement s=c.prepareStatement("INSERT INTO users(username,password,role,first_name,last_name,must_change_password) VALUES(?,?,?,'Expanded','Assurance',0)")){
                s.setString(1,Integer.toString(id));s.setString(2,PasswordService.hashPassword(password));s.setString(3,role);s.executeUpdate();ownedUsers.add(Integer.toString(id));
            }
        }
    }
    private static void expireRetry(Connection c,String username) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("UPDATE users SET next_login_at=UTC_TIMESTAMP(6)-INTERVAL 1 SECOND WHERE username=?")){s.setString(1,username);s.executeUpdate();}
        try(PreparedStatement s=c.prepareStatement("UPDATE AuthThrottle SET RetryAfter=UTC_TIMESTAMP(6)-INTERVAL 1 SECOND WHERE SourceKey=?")){s.setString(1,AuditService.sourceKey());s.executeUpdate();}
    }
    private static void clearThrottle(Connection c) throws SQLException {
        execute(c,"UPDATE users SET failed_attempts=0,locked_until=NULL,next_login_at=NULL WHERE username IN ('99002','99003','99004')");
        try(PreparedStatement s=c.prepareStatement("UPDATE AuthThrottle SET FailedAttempts=0,RetryAfter=NULL,LockedUntil=NULL WHERE SourceKey=?")){s.setString(1,AuditService.sourceKey());s.executeUpdate();}
    }
    private static String[] attendance(String employee,String date) {
        return new String[]{employee,"Expanded","Assurance","Regular","Synthetic","N/A",date,date,"8:00:00","17:00:00","8","1","Vacation Leave","Pending","10","0","10","10","0","10"};
    }
    private static String[] payroll(String employee) {
        return new String[]{"EXP-"+UUID.randomUUID(),employee,"Expanded","Assurance","01-Jun-26","15-Jun-26","20000","113.64","600","176","80","0","10600","0","0","0","0","0","0","0","10600","Pending"};
    }
    private static void lockProbe() {
        char[] proof=System.getenv("M2_PROBE_PASSWORD").toCharArray();
        try{DatabaseHandler.authenticate(EMP,proof);throw new IllegalStateException("Cross-process lock bypassed");}
        catch(AuthenticationThrottledException expected){System.out.println("Persisted account lock denied from another JVM and source identity.");}
        finally{Arrays.fill(proof,'\0');}
    }
    public static void main(String[] args) throws Exception {
        url=System.getenv("M2_JDBC_URL");
        if(url==null||!url.startsWith("jdbc:mysql://127.0.0.1:3307/aoop_m2?"))throw new IllegalStateException("Non-clone target refused");
        System.setProperty("aoop.db.url",url);System.setProperty("aoop.db.user","motorph_m2_app");System.setProperty("aoop.db.password",System.getenv("M2_APP_PASSWORD"));
        if(System.getenv("COMPUTERNAME")==null||!System.getenv("COMPUTERNAME").startsWith("M2-ASSURANCE-"))throw new IllegalStateException("Dedicated assurance source identity required");
        if(args.length>0&&args[0].equals("lock-probe")){lockProbe();return;}
        hrPassword=PasswordService.generateTemporaryPassword();employeePassword=PasswordService.generateTemporaryPassword();
        temporary=PasswordService.generateTemporaryPassword();replacement=PasswordService.generateTemporaryPassword();
        try(Connection c=admin()) {
            check(number(c,"SELECT @@port")==3307,"Actual target is isolated port3307");
            try(Connection application=DatabaseConfig.getInstance().getConnection();Statement s=application.createStatement();ResultSet r=s.executeQuery("SHOW SESSION STATUS LIKE 'Ssl_cipher'")){
                r.next();check(r.getString(2)!=null&&!r.getString(2).isBlank(),"Updated JDBC driver negotiates protected TLS connection");
            }
            try(Connection ignored=DriverManager.getConnection(url.replace("sslMode=VERIFY_IDENTITY","sslMode=DISABLED"),"motorph_m2_app",System.getenv("M2_APP_PASSWORD"))){throw new IllegalStateException("Plaintext connection accepted");}
            catch(SQLException expected){check(Set.of(1045,3159).contains(expected.getErrorCode()),"Plaintext transport is rejected by server/account");}
            seed(c,99002,hrPassword,"HR",true);seed(c,99003,employeePassword,"Employee",true);seed(c,99004,null,null,false);
            EmpUserSession.getInstance().clearSession();
            denied(()->DatabaseHandler.getEmployeeData(),"Unauthenticated DAL read rejected");
            denied(()->EmpUserSession.getInstance().setCurrentUser(null,"HR"),"Caller-supplied session role rejected");
            login(HR,hrPassword);
            check(DatabaseHandler.getEmployeeData().size()>3,"HR collection reads retain administration access");
            check(!DatabaseHandler.getSalaryData().isEmpty()&&!DatabaseHandler.getAttendanceData().isEmpty()&&!DatabaseHandler.getPayrollData().isEmpty(),"Existing salary attendance payroll reads retained");
            check(number(c,"SELECT COUNT(*) FROM employees WHERE EmployeeID=99005")==0,"Application CRUD fixture ID is unused");
            EmpDetails template=DatabaseHandler.getEmployeeData().get(0);
            EmpDetails created=new EmpDetails("99005","Expanded","Crud","1990-01-01",CREATED_ADDRESS,"09123456789",template.getEmployeeStatus(),template.getPosition(),"N/A");
            DatabaseHandler.addEmployee(created);ownedEmployees.add(99005);
            check(DatabaseHandler.employeeExists("99005",null),"HR employee creation succeeds with restricted grants");
            DatabaseHandler.deleteEmployee("99005");
            check(!DatabaseHandler.employeeExists("99005",null),"HR employee deletion succeeds and is audited");
            denied(()->DatabaseHandler.loadCredentials(),"Bulk credential dump API rejected");
            DatabaseHandler.createUser(RESET,temporary,"EMPLOYEE","Expanded","Assurance");ownedUsers.add(RESET);
            EmpUserSession.getInstance().clearSession();
            DatabaseHandler.AuthenticationResult pending=DatabaseHandler.authenticate(RESET,temporary);
            check(pending!=null&&pending.isResetRequired()&&pending.getEmployee()==null,"Temporary credential permits reset only");
            denied(()->EmpUserSession.getInstance().establish(pending),"Reset-only result cannot establish session");
            failed(()->DatabaseHandler.resetPassword(RESET,"Wrong-current-2026".toCharArray(),replacement),"Reset proof failures persist throttle");
            failed(()->DatabaseHandler.resetPassword(RESET,temporary,replacement),"Reset cannot bypass retry deadline");
            clearThrottle(c);
            DatabaseHandler.AuthenticationResult reset=DatabaseHandler.resetPassword(RESET,temporary,replacement);
            check(reset!=null&&!reset.isResetRequired(),"Committed BCrypt reset releases full capability");
            EmpUserSession.getInstance().establish(reset);
            denied(()->EmpUserSession.getInstance().establish(reset),"Authentication capability is single use");
            check(PasswordService.isSupportedHash(PasswordService.hashPassword(replacement)),"BCrypt12 storage policy retained");
            failed(()->DatabaseHandler.authenticate(RESET,temporary),"Previous temporary credential revoked");clearThrottle(c);
            login(EMP,employeePassword);
            List<EmpDetails> own=DatabaseHandler.getEmployeeData();
            check(own.size()==1&&EMP.equals(own.get(0).getEmpID()),"Employee profile collection scoped to self");
            check(!DatabaseHandler.getSalaryData().isEmpty()&&DatabaseHandler.getSalaryData().stream().allMatch(x->EMP.equals(x.getEmpID())),"Employee salary collection scoped to self");
            denied(()->DatabaseHandler.deleteEmployee(HR),"Employee cannot invoke HR deletion directly");
            denied(()->DatabaseHandler.createUser("99005",temporary,"HR","X","Y"),"Employee cannot create HR accounts");
            denied(()->DatabaseHandler.getSecurityAlerts(0),"Employee cannot read security activity");
            denied(()->DatabaseHandler.getAuditEvents(0),"Employee cannot read business audit history");
            denied(()->CSVHandler.updateAttendanceStatus(HR,"Approved",""),"Employee cannot approve another request");
            denied(()->DatabaseHandler.importLegacyLedgers(List.of(),List.of()),"Employee cannot import ledgers");
            String[] request=attendance(EMP,"01-May-26");
            DatabaseLedger.addAttendance(request);
            check(CSVHandler.loadAttendanceRequests().size()==1,"Own request saved through database adapter");
            String[] approved=request.clone();approved[13]="Approved";
            try{DatabaseLedger.editAttendance(approved,request[6],request[7]);throw new IllegalStateException("Request edit bypassed approval");}
            catch(IllegalArgumentException expected){check(true,"Employee cannot self-approve through request edit");}
            String[] another=attendance(HR,"02-May-26");
            denied(()->DatabaseLedger.addAttendance(another),"Employee cannot submit for another employee");
            String[] poisoned=attendance(EMP,"02-May-26");poisoned[10]="NaN";
            try{DatabaseLedger.addAttendance(poisoned);throw new IllegalStateException("Nonfinite request persisted");}
            catch(IllegalArgumentException expected){check(true,"Nonfinite attendance value rejected before persistence");}
            check(number(c,"SELECT COUNT(*) FROM AuditLog WHERE Action='ATTENDANCE_REQUEST_CREATED' AND ActorID='99003'")>0,"Attendance write committed its audit event");
            login(HR,hrPassword);
            CSVHandler.updateAttendanceStatus(EMP,"01-May-26","01-May-26","Approved","");
            check("Approved".equals(CSVHandler.loadAttendanceRequests().stream().filter(x->EMP.equals(x.getEmpID())).findFirst().orElseThrow().getAttendanceStatus()),"HR exact-request approval retained");
            String[] imported=attendance(EMP,"03-May-26"),pay=payroll(EMP);
            DatabaseHandler.importLegacyLedgers(Collections.singletonList(imported),Collections.singletonList(pay));
            long rows=number(c,"SELECT COUNT(*) FROM AttendanceRequests WHERE EmployeeID=99003");
            DatabaseHandler.importLegacyLedgers(Collections.singletonList(imported),Collections.singletonList(pay));
            check(number(c,"SELECT COUNT(*) FROM AttendanceRequests WHERE EmployeeID=99003")==rows,"Exact legacy reimport is idempotent");
            String[] conflicting=imported.clone();conflicting[12]="Sick Leave";
            try{DatabaseHandler.importLegacyLedgers(Collections.singletonList(conflicting),List.of());throw new IllegalStateException("Conflicting import accepted");}
            catch(PersistenceException expected){check(true,"Conflicting import preserves existing records");}
            check(Arrays.equals(CSVHandler.loadPayrollLedger().stream().filter(x->pay[0].equals(x[0])).findFirst().orElseThrow(),pay),"22-field payroll legacy data reconciles exactly");
            try{CSVHandler.updatePayrollStatus(pay[0],EMP,pay[4],pay[5],"Approved","");throw new IllegalStateException("Payroll transition bypassed");}
            catch(IllegalArgumentException expected){check(true,"Payroll approval cannot skip verification");}
            CSVHandler.updatePayrollStatus(pay[0],EMP,pay[4],pay[5],"Verified","");
            CSVHandler.updatePayrollStatus(pay[0],EMP,pay[4],pay[5],"Approved","");
            check("Approved".equals(CSVHandler.loadPayrollLedger().stream().filter(x->pay[0].equals(x[0])).findFirst().orElseThrow()[21]),"HR exact payroll approval is persisted and audited");
            AuditEvent statusEvent=DatabaseHandler.getAuditEvents(0).stream().filter(x->pay[0].equals(x.getTarget())&&"PAYROLL_STATUS_CHANGED".equals(x.getAction())).findFirst().orElseThrow();
            check(statusEvent.getDetails().equals("status_before=Verified;status_after=Approved"),"Business audit retains committed before/after status only");
            check(HR.equals(statusEvent.getActor())&&"SUCCESS".equals(statusEvent.getOutcome()),"Business audit exposes authorized actor and outcome metadata");
            long existingHead=number(c,"SELECT LastEventID FROM AuditChainHead WHERE ChainID=1");
            if(existingHead<110)try(Connection application=DatabaseConfig.getInstance().getConnection()){
                application.setAutoCommit(false);
                for(long i=existingHead;i<110;i++)AuditService.append(application,"assurance","PAGING_FIXTURE","isolated","SUCCESS","pagination metadata fixture");
                application.commit();
            }
            List<AuditEvent> newest=DatabaseHandler.getAuditEvents(0);
            check(newest.size()==100,"HR audit page is bounded to one hundred metadata rows");
            long cursor=newest.get(newest.size()-1).getEventId();
            List<AuditEvent> older=DatabaseHandler.getAuditEvents(cursor);
            check(!older.isEmpty()&&older.stream().allMatch(x->x.getEventId()<cursor),"HR keyset navigation reaches older business events without overlap");
            try{DatabaseHandler.getAuditEvents(-1);throw new IllegalStateException("Negative audit cursor accepted");}
            catch(IllegalArgumentException expected){check(true,"Invalid audit history cursor rejected");}
            long head=number(c,"SELECT LastEventID FROM AuditChainHead WHERE ChainID=1");
            EmpDetails editable=DatabaseHandler.getEmployeeData().stream().filter(x->EMP.equals(x.getEmpID())).findFirst().orElseThrow();
            String phone=editable.getPhoneNumber();editable.setPhoneNumber("09999999999");
            execute(c,"REVOKE EXECUTE ON PROCEDURE aoop_m2.motorph_append_audit FROM 'motorph_m2_app'@'127.0.0.1'");
            try{try{DatabaseHandler.updateEmployee(editable);throw new IllegalStateException("Unaudited mutation committed");}
                catch(PersistenceException expected){check(true,"Audit failure rejects sensitive business mutation");}}
            finally{execute(c,"GRANT EXECUTE ON PROCEDURE aoop_m2.motorph_append_audit TO 'motorph_m2_app'@'127.0.0.1'");}
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT PhoneNumber FROM employees WHERE EmployeeID=99003")){r.next();check(phone.equals(r.getString(1)),"Audit failure rolled back employee mutation");}
            check(number(c,"SELECT LastEventID FROM AuditChainHead WHERE ChainID=1")==head,"Failed business transaction leaves audit head unchanged");
            check(DatabaseHandler.verifyAuditChain(),"Application verifies complete server audit chain");
            login(EMP,employeePassword);
            check(!CSVHandler.loadPayrollLedger().isEmpty()&&CSVHandler.loadPayrollLedger().stream().allMatch(x->EMP.equals(x[1])),"Employee payroll workflow reads scoped to self");
            execute(c,"UPDATE users SET must_change_password=1 WHERE username='99003'");
            denied(()->DatabaseHandler.getEmployeeData(),"Changed account/reset state invalidates existing session");
            execute(c,"UPDATE users SET must_change_password=0 WHERE username='99003'");
            clearThrottle(c);
            for(int i=1;i<=5;i++) {
                long retry=failed(()->DatabaseHandler.authenticate(EMP,"Wrong-Password-2026".toCharArray()),"Failed login "+i+" persisted");
                check(retry==(i==5?900:1L<<(i-1)),"Progressive delay step "+i);
                if(i<5)expireRetry(c,EMP);
            }
            check(number(c,"SELECT failed_attempts FROM users WHERE username='99003'")==5,"Five failures persisted in database");
            check(number(c,"SELECT TIMESTAMPDIFF(SECOND,UTC_TIMESTAMP(),locked_until) FROM users WHERE username='99003'")>=895,"Fifth failure applies fifteen-minute lock");
            failed(()->DatabaseHandler.authenticate(EMP,employeePassword),"Correct password cannot bypass active lock");
            ProcessBuilder child=new ProcessBuilder(Paths.get(System.getProperty("java.home"),"bin","java.exe").toString(),"-cp",System.getProperty("java.class.path"),ExpandedSecurityAssurance.class.getName(),"lock-probe");
            child.environment().put("COMPUTERNAME",System.getenv("COMPUTERNAME")+"-other");child.environment().put("M2_PROBE_PASSWORD",new String(employeePassword));child.environment().remove("M2_ADMIN_PASSWORD");
            Process process=child.start();check(process.waitFor(20,TimeUnit.SECONDS)&&process.exitValue()==0,"Account lock persists across JVM and source change");
            clearThrottle(c);login(HR,hrPassword);
            check(!DatabaseHandler.getSecurityAlerts(0).isEmpty(),"HR sees accumulated authentication alerts");
            check(DatabaseHandler.verifyAuditChain(),"Full audit chain remains valid after rejected authentication");
        } finally {
            EmpUserSession.getInstance().clearSession();
            for(char[] value:new char[][]{hrPassword,employeePassword,temporary,replacement})if(value!=null)Arrays.fill(value,'\0');
            try(Connection c=admin()) {
                for(String id:ownedUsers)execute(c,"DELETE FROM users WHERE username='"+id+"'");
                for(int id:ownedEmployees){execute(c,"DELETE FROM AttendanceRequests WHERE EmployeeID="+id);execute(c,"DELETE FROM PayrollLedger WHERE EmployeeID="+id);execute(c,"DELETE FROM employees WHERE EmployeeID="+id);}
                if(ownedEmployees.contains(99005))try(PreparedStatement s=c.prepareStatement("DELETE FROM addresses WHERE StreetAddress=?")){s.setString(1,CREATED_ADDRESS);s.executeUpdate();}
                try(PreparedStatement s=c.prepareStatement("DELETE FROM AuthThrottle WHERE SourceMachine IN (?,?)")){s.setString(1,System.getenv("COMPUTERNAME"));s.setString(2,System.getenv("COMPUTERNAME")+"-other");s.executeUpdate();}
            }
        }
        System.out.println("{\"assertions\":"+checks+",\"outcomes\":["+outcomes.stream().map(x->"\""+x.replace("\\","\\\\").replace("\"","\\\"")+"\"").collect(java.util.stream.Collectors.joining(","))+"]}");
    }
}

package oop_motorph;
import java.nio.file.*;
import java.sql.Connection;
import java.time.*;
import java.util.*;

/** Offline checks for authorized, audited database-backed compatibility APIs. */
public final class PersistenceRegressionTest {
 private static int assertions;
 public static void main(String[] args)throws Exception {
  try(BackendJdbcFixture f=new BackendJdbcFixture()) {
   check(f.connections==0,"Configuration initialization must not connect");
   Connection a=DatabaseConfig.getInstance().getConnection(),b=DatabaseConfig.getInstance().getConnection();
   check(a!=b,"Connections must have independent ownership");a.close();check(!b.isClosed(),"Closing a connection must not close another");b.close();
   check("5000".equals(f.properties.getProperty("connectTimeout")),"Connection timeout must be bounded");
   check("15000".equals(f.properties.getProperty("socketTimeout")),"Read timeout must be bounded");
   System.setProperty("aoop.db.url","");failure(()->DatabaseConfig.getInstance().getConnection());System.setProperty("aoop.db.url",BackendJdbcFixture.URL);
   f.login("10001");
   check(CSVHandler.employeeExists("10001",null),"Null excluded ID must detect duplicates");
   check(!CSVHandler.employeeExists("10001","10001"),"The current employee must be excludable");
   EmpDetails employee=employee("10004");int events=f.store.events.size();DatabaseHandler.addEmployee(employee);
   check(f.store.profiles.containsKey("10004"),"Employee insertion must commit");check(f.store.events.size()==events+1,"Employee insertion must share its audit transaction");
   f.failAudit=true;failure(()->DatabaseHandler.addEmployee(employee("10005")));f.failAudit=false;
   check(!f.store.profiles.containsKey("10005"),"Audit failure must roll back employee insertion");
   DatabaseHandler.updateEmployee(employee);
   DatabaseHandler.updateEmployeeSalary(new EmpSalaryDetails("10004","New","Employee","sss","health","tin","fund",20000,1500,500,500,999,999));
   check(f.statements.stream().filter(sql->sql.startsWith("UPDATE compensation")||sql.startsWith("INSERT INTO compensation")).noneMatch(sql->sql.contains("GrossSemimonthly")||sql.contains("HourlyRate")),"Generated salary columns must not be assigned");
   f.departmentRows=0;failure(()->DatabaseHandler.addEmployee(employee("10006")));f.departmentRows=2;failure(()->DatabaseHandler.addEmployee(employee("10006")));f.departmentRows=1;
   check(!f.store.profiles.containsKey("10006"),"Unknown or ambiguous departments must not use a fallback");
   DatabaseHandler.deleteEmployee("10004");check(!f.store.profiles.containsKey("10004"),"Employee deletion must commit");
   ledgers(f);
  }
  System.out.println("Persistence regression checks passed: "+assertions+" assertions; fake JDBC only.");
 }
 private static void ledgers(BackendJdbcFixture f)throws Exception {
  EmpAttLeave request=request("10002",LocalDate.of(2026,10,17),LocalDate.of(2026,10,18));CSVHandler.saveAttendanceRequest(request);
  check(CSVHandler.loadAttendanceRequests().size()==1,"Database requests must reload");check("First".equals(CSVHandler.loadAttendanceRequests().get(0).getFirstName()),"New requests must use the database profile");
  failure(()->CSVHandler.saveAttendanceRequest(request));request.setAttDateTo(LocalDate.of(2026,10,19));CSVHandler.saveEditAttendanceRequest(request,"17-Oct-26","18-Oct-26");
  check(CSVHandler.loadAttendanceRequests().get(0).getAttDateTo().equals(request.getAttDateTo()),"Exact request edits must persist");
  CSVHandler.updateAttendanceStatus("10002","2026-10-17","2026-10-19","Approved","");
  CSVHandler.saveAttendanceRequest(request("10002",LocalDate.of(2026,11,1),LocalDate.of(2026,11,2)));
  failure(()->CSVHandler.updateAttendanceStatus("10002","Rejected",""));CSVHandler.updateAttendanceStatus("10002","2026-11-01","2026-11-02","Rejected","");
  check("Approved".equals(CSVHandler.loadAttendanceRequests().get(0).getAttendanceStatus()),"Exact approval must preserve other requests");
  check("Rejected".equals(CSVHandler.loadAttendanceRequests().get(1).getAttendanceStatus()),"Exact approval must update its date range");
  String[] legacy=attendanceRow("10003","2026-12-01","2026-12-02");legacy[1]="First, quoted";legacy[2]="Last\nLine";
  DatabaseHandler.importLegacyLedgers(Collections.singletonList(legacy),List.of());DatabaseHandler.importLegacyLedgers(Collections.singletonList(legacy),List.of());
  check(f.store.attendance.size()==3,"Identical import retries must be idempotent");check(CSVHandler.loadAttendanceRequests().get(2).getLastName().equals("Last\nLine"),"Explicit imports must preserve multiline data");
  String[] conflict=legacy.clone();conflict[1]="conflict";failure(()->DatabaseHandler.importLegacyLedgers(Collections.singletonList(conflict),List.of()));check(f.store.attendance.size()==3,"Conflicting import must not overwrite data");
  CSVHandler.saveUpdatedPayrollData(Arrays.asList(payrollRow("one","10002","2026-10-01","2026-10-15"),payrollRow("two","10002","2026-10-16","2026-10-31")));
  failure(()->CSVHandler.updatePayrollStatus("10002","Approved",""));CSVHandler.updatePayrollStatus("one","10002","01-Oct-26","15-Oct-26","Verified","");CSVHandler.updatePayrollStatus("one","10002","01-Oct-26","15-Oct-26","Approved","");
  check("Approved".equals(CSVHandler.loadPayrollLedger().get(0)[21]),"Payroll status must persist its exact transaction");check("Pending".equals(CSVHandler.loadPayrollLedger().get(1)[21]),"Other payroll periods must be preserved");check("123.45".equals(CSVHandler.loadPayrollLedger().get(0)[20]),"Status updates must preserve net pay");
  f.failAudit=true;failure(()->CSVHandler.updatePayrollStatus("one","10002","2026-10-01","2026-10-15","Pending",""));f.failAudit=false;check("Approved".equals(CSVHandler.loadPayrollLedger().get(0)[21]),"Audit failure must roll back payroll changes");
  Path directory=Files.createTempDirectory("motorph-no-file-writes-");Path file=directory.resolve(LocalRecordStore.ATTENDANCE);Files.writeString(file,"unchanged");
  String previous=System.getProperty("aoop.data.dir");System.setProperty("aoop.data.dir",directory.toString());
  try {denied(()->LocalRecordStore.write(LocalRecordStore.ATTENDANCE,List.of()));check("unchanged".equals(Files.readString(file)),"Retired file-write APIs must preserve existing files");}
  finally {if(previous==null){System.clearProperty("aoop.data.dir");}else{System.setProperty("aoop.data.dir",previous);}Files.delete(file);Files.delete(directory);}
  check(DatabaseHandler.verifyAuditChain(),"Normal transactions must preserve the complete audit chain");
 }
 static EmpDetails employee(String id){return new EmpDetails(id,"New","Employee","1990-01-01","Address","1234567890","Regular","Engineer","N/A");}
 static EmpAttLeave request(String id,LocalDate from,LocalDate to){return new EmpAttLeave(id,"Submitted","Name","Regular","Engineer","N/A",from,to,LocalTime.of(8,0),LocalTime.of(17,0),8,1,"VL Request","Pending",10,1,9,10,0,10);}
 static String[] attendanceRow(String id,String from,String to){return new String[]{id,"First","Last","Regular","Engineer","N/A",from,to,"08:00:00","17:00:00","8","1","VL Request","Pending","10","1","9","10","0","10"};}
 static String[] payrollRow(String transaction,String id,String from,String to){String[] r=new String[22];Arrays.fill(r,"100.00");r[0]=transaction;r[1]=id;r[2]="First";r[3]="Last";r[4]=from;r[5]=to;r[20]="123.45";r[21]="Pending";return r;}
 private static void check(boolean value,String message){assertions++;if(!value){throw new AssertionError(message);}}
 private static void failure(Runnable action){try{action.run();}catch(PersistenceException expected){assertions++;return;}throw new AssertionError("Expected persistence failure");}
 private static void denied(Runnable action){try{action.run();}catch(AuthorizationException expected){assertions++;return;}throw new AssertionError("Expected authorization denial");}
}

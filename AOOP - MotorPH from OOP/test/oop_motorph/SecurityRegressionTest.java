package oop_motorph;
import java.time.*;
import java.util.*;

/** Password, persisted throttle, authorization and audit regressions using fake JDBC only. */
public final class SecurityRegressionTest {
 private static int assertions;
 private static final char[] TEMPORARY="Unique-temporary-proof!".toCharArray();
 private static final char[] PERSONAL=" Personal-password-2026! ".toCharArray();
 public static void main(String[] args)throws Exception {
  passwordStorageAndBoundaries();
  try(BackendJdbcFixture f=new BackendJdbcFixture()) {forcedReset(f);persistedThrottle(f);authorization(f);retiredMigration(f);auditViewer(f);auditIntegrity(f);}
  System.out.println("Security regression checks passed: "+assertions+" assertions; BCrypt12, authorization and transactional fake JDBC.");
 }
    private static void passwordStorageAndBoundaries() {
        String first = PasswordService.hashPassword(PERSONAL);
        String second = PasswordService.hashPassword(PERSONAL);
        check(first.startsWith("$2b$12$") && first.length() == 60, "New credentials must use BCrypt12");
        check(!first.equals(second), "Each hash must have a unique random salt");
        check(PasswordService.verifyPassword(PERSONAL, first), "The exact correct password must verify");
        check(!PasswordService.verifyPassword("Personal-password-2026!".toCharArray(), first), "Password whitespace must remain significant");
        check(!PasswordService.verifyPassword("Wrong-personal-password!".toCharArray(), first), "Incorrect passwords must fail");
        check(!PasswordService.verifyPassword(PERSONAL, "f".repeat(64)), "Legacy SHA-256 must never be accepted or wrapped");
        expectPolicy(() -> PasswordService.validateNewPassword("short".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("123412341234".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("password1234".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("x".repeat(73).toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("abcdefghi😀😀😀😀😀😀😀😀😀😀😀😀😀😀😀😀".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("😀😀😀😀😀😀".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword(("Unicode-proof-" + '\uD800').toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("control\npassword!".toCharArray()));
        char[] maximum = ("A".repeat(71) + "Z").toCharArray();
        String maximumHash = PasswordService.hashPassword(maximum);
        check(PasswordService.verifyPassword(maximum, maximumHash), "Exactly72 UTF-8 bytes must be supported");
        maximum[71] = 'Y';
        check(!PasswordService.verifyPassword(maximum, maximumHash), "The72nd byte must not be truncated");
        char[] firstTemporary = PasswordService.generateTemporaryPassword();
        char[] secondTemporary = PasswordService.generateTemporaryPassword();
        PasswordService.validateNewPassword(firstTemporary);
        check(!Arrays.equals(firstTemporary, secondTemporary), "Temporary account credentials must be unique");
        Arrays.fill(firstTemporary, '\0'); Arrays.fill(secondTemporary, '\0'); Arrays.fill(maximum, '\0');
    }


 private static void forcedReset(BackendJdbcFixture f) {
  f.account("10002").hash=PasswordService.hashPassword(TEMPORARY);f.account("10002").change=true;
  DatabaseHandler.AuthenticationResult pending=DatabaseHandler.authenticate("10002",TEMPORARY);
  check(pending!=null&&pending.isResetRequired(),"Temporary proof must enter reset-only state");
  check(pending.getEmployee()==null&&pending.getRole()==null,"Reset-only state must expose no full-session profile or role");
  denied(()->EmpUserSession.getInstance().establish(pending));
  AuthenticationThrottledException wrong=throttled(()->DatabaseHandler.resetPassword("10002","wrong-temporary".toCharArray(),PERSONAL));
  check(wrong.getRetryAfterSeconds()==1&&f.account("10002").failures==1,"Wrong reset proof must persist its first retry interval");f.advance(1);
  expectPolicy(()->DatabaseHandler.resetPassword("10002",TEMPORARY,TEMPORARY));
  DatabaseHandler.AuthenticationResult full=DatabaseHandler.resetPassword("10002",TEMPORARY,PERSONAL);
  check(full!=null&&!full.isResetRequired(),"Only a committed reset may return full authentication");
  EmpUserSession.getInstance().establish(full);check("10002".equals(EmpUserSession.getInstance().getCurrentUser().getEmpID()),"Committed reset capability must establish its actual identity");
  denied(()->EmpUserSession.getInstance().establish(full));
  check(!f.account("10002").change&&PasswordService.verifyPassword(PERSONAL,f.account("10002").hash),"Reset must persist BCrypt and clear the flag together");
  throttled(()->DatabaseHandler.authenticate("10002",TEMPORARY));f.advance(1);
  check(DatabaseHandler.authenticate("10002",PERSONAL)!=null,"New personal password must authenticate after the retry interval");
  f.account("10002").hash=PasswordService.hashPassword(BackendJdbcFixture.PASSWORD);f.clearThrottle();EmpUserSession.getInstance().clearSession();
 }
 private static void persistedThrottle(BackendJdbcFixture f) {
  check(!AuthenticationService.verifyCredential(BackendJdbcFixture.PASSWORD,null),"Unknown accounts must perform dummy verification without accepting the proof");
  check(!AuthenticationService.verifyCredential(BackendJdbcFixture.PASSWORD,"0".repeat(64)),"Unsupported legacy hashes must use the same dummy-verification path");
  f.clearThrottle();
  for(int i=1;i<=5;i++) {
   f.store.sources.clear(); // Simulates a fresh source: account-wide state must still persist.
   AuthenticationThrottledException failure=throttled(()->DatabaseHandler.authenticate("10002","wrong-password".toCharArray()));
   long expected=i==5?900:1L<<(i-1);check(failure.getRetryAfterSeconds()==expected,"Persisted account retry intervals must be1/2/4/8 then15minutes");
   check(f.account("10002").failures==i,"Rejected attempts must commit their account counter");
   check(throttled(()->DatabaseHandler.authenticate("10002",BackendJdbcFixture.PASSWORD)).getRetryAfterSeconds()==expected,"Correct proof must be denied during retry or lockout");
   check(f.account("10002").failures==i,"Blocked retries must not add another failed-proof counter");
   if(i<5){f.advance(expected);}
  }
  f.store.sources.clear();f.advance(899);check(throttled(()->DatabaseHandler.authenticate("10002",BackendJdbcFixture.PASSWORD)).getRetryAfterSeconds()==1,"A fresh source must not bypass a persisted account lock");
  f.advance(1);check(DatabaseHandler.authenticate("10002",BackendJdbcFixture.PASSWORD)!=null,"Expired account lock must permit valid proof");check(f.account("10002").failures==0,"Successful authentication after lock expiry must clear counters");
  f.clearThrottle();
  for(int i=1;i<=5;i++){final String missing="missing-"+i;long seconds=i==5?900:1L<<(i-1);check(throttled(()->DatabaseHandler.authenticate(missing,BackendJdbcFixture.PASSWORD)).getRetryAfterSeconds()==seconds,"Source throttle must combine unknown usernames");if(i<5){f.advance(seconds);}}
  check(throttled(()->DatabaseHandler.authenticate("10001",BackendJdbcFixture.PASSWORD)).getRetryAfterSeconds()==900,"Source lock must also block correct proof on another account");
  f.clearThrottle();int events=f.store.events.size();f.failAudit=true;expectPersistence(()->DatabaseHandler.authenticate("10002","wrong-password".toCharArray()));f.failAudit=false;
  check(f.account("10002").failures==0&&f.store.events.size()==events,"Failed authentication audit must roll back counters and events together");
 }
 private static void authorization(BackendJdbcFixture f) {
  EmpUserSession session=EmpUserSession.getInstance();session.clearSession();int connections=f.connections;
  denied(DatabaseHandler::getEmployeeData);denied(DatabaseHandler::getSalaryData);denied(DatabaseHandler::getAttendanceData);denied(DatabaseHandler::getPayrollData);denied(CSVHandler::loadAttendanceRequests);denied(CSVHandler::loadPayrollLedger);
  check(f.connections==connections,"Unauthenticated DAL calls must fail before opening connections");
  denied(()->session.setCurrentUser(new EmpDetails("10001","Forged","HR"),"HR"));
  denied(()->session.establish(new DatabaseHandler.AuthenticationResult(new EmpDetails("10001","Forged","HR"),"HR")));
  f.login("10002");
  check(DatabaseHandler.getEmployeeData().size()==1&&"10002".equals(DatabaseHandler.getEmployeeData().get(0).getEmpID()),"Employee list reads must contain only the actual signed-in employee");
  check(DatabaseHandler.getSalaryData().size()==1&&"10002".equals(DatabaseHandler.getSalaryData().get(0).getEmpID()),"Salary reads must be scoped to the actual employee");
  session.getCurrentUser().setEmpID("10003");
  check("10002".equals(DatabaseHandler.getEmployeeData().get(0).getEmpID()),"Mutating the displayed profile must not forge the immutable authorization identity");
  denied(()->DatabaseHandler.addEmployee(PersistenceRegressionTest.employee("10007")));
  denied(()->DatabaseHandler.updateEmployee(PersistenceRegressionTest.employee("10002")));
  denied(()->DatabaseHandler.getSecurityAlerts(0));denied(DatabaseHandler::verifyAuditChain);denied(DatabaseHandler::getAllSupervisors);denied(DatabaseHandler::loadCredentials);
  denied(()->CSVHandler.saveAttendanceRequest(PersistenceRegressionTest.request("10003",LocalDate.of(2027,1,1),LocalDate.of(2027,1,2))));
  CSVHandler.saveAttendanceRequest(PersistenceRegressionTest.request("10002",LocalDate.of(2027,1,1),LocalDate.of(2027,1,2)));
  check(CSVHandler.loadAttendanceRequests().stream().allMatch(r->r.getEmpID().equals("10002")),"Request reads must enforce employee object scope");
  denied(()->CSVHandler.updateAttendanceStatus("10002","2027-01-01","2027-01-02","Approved",""));
  denied(()->CSVHandler.saveUpdatedPayrollData(List.of()));
  f.account("10002").change=true;denied(DatabaseHandler::getEmployeeData);check(session.getCurrentUser()==null,"Forced-reset state changes must revoke an existing full session");f.account("10002").change=false;
  f.login("10001");check(DatabaseHandler.getEmployeeData().size()>=3,"HR must retain administrative list access");
  check(!DatabaseHandler.getSecurityAlerts(0).isEmpty(),"HR security alerts must expose committed authentication failures");
  int rows=f.store.attendance.size(),events=f.store.events.size();
  EmpAttLeave invalid=PersistenceRegressionTest.request("10002",LocalDate.of(2027,2,1),LocalDate.of(2027,2,2));invalid.setHoursWorked(Double.NaN);expectPolicy(()->CSVHandler.saveAttendanceRequest(invalid));
  check(f.store.attendance.size()==rows&&f.store.events.size()==events,"Invalid attendance numbers must not persist or append success audit");
  CSVHandler.updateAttendanceStatus("10002","2027-01-01","2027-01-02","Approved","");
  expectPolicy(()->CSVHandler.updateAttendanceStatus("10002","2027-01-01","2027-01-02","Rejected",""));
  EmpAttLeave edited=PersistenceRegressionTest.request("10002",LocalDate.of(2027,1,1),LocalDate.of(2027,1,2));expectPolicy(()->CSVHandler.saveEditAttendanceRequest(edited,"2027-01-01","2027-01-02"));
  String[] pay=PersistenceRegressionTest.payrollRow("security-pay","10002","2027-02-01","2027-02-15");DatabaseLedger.savePayroll(pay);
  String[] invalidPayroll=pay.clone();invalidPayroll[0]="invalid-numeric";invalidPayroll[6]="Infinity";int payrollCount=f.store.payroll.size(),auditCount=f.store.events.size();expectPolicy(()->DatabaseLedger.savePayroll(invalidPayroll));check(f.store.payroll.size()==payrollCount&&f.store.events.size()==auditCount,"Invalid payroll numbers must not persist or append success audit");
  expectPolicy(()->CSVHandler.updatePayrollStatus("security-pay","10002","2027-02-01","2027-02-15","Approved",""));
  CSVHandler.updatePayrollStatus("security-pay","10002","2027-02-01","2027-02-15","Verified","");
  String[] bypass=pay.clone();bypass[21]="Approved";expectPolicy(()->DatabaseLedger.savePayroll(bypass));expectPolicy(()->CSVHandler.saveUpdatedPayrollData(Collections.singletonList(bypass)));
  CSVHandler.updatePayrollStatus("security-pay","10002","2027-02-01","2027-02-15","Approved","");
  CSVHandler.updatePayrollStatus("security-pay","10002","2027-02-01","2027-02-15","Pending","");
  check("Pending".equals(CSVHandler.loadPayrollLedger().get(0)[21]),"Explicit payroll undo transition must preserve the existing workflow");
 }
 private static void retiredMigration(BackendJdbcFixture f) {
  EmpUserSession.getInstance().clearSession();int connections=f.connections;
  denied(DataMigration::generatePayrollFromAttendance);denied(()->DataMigration.main(new String[0]));
  check(f.connections==connections,"Unauthenticated retired migration entry points must refuse before JDBC");
  f.login("10001");connections=f.connections;int commits=f.commits,events=f.store.events.size(),profiles=f.store.profiles.size(),payroll=f.store.payroll.size();
  expectPersistence(DataMigration::generatePayrollFromAttendance);expectPersistence(()->DataMigration.main(new String[0]));
  check(f.connections==connections&&f.commits==commits,"Retired migration must not open JDBC or begin partial writes even for HR");
  check(f.store.events.size()==events&&f.store.profiles.size()==profiles&&f.store.payroll.size()==payroll,"Retired CLI must leave business records and audit history unchanged");
 }
 private static void auditViewer(BackendJdbcFixture f) {
  EmpUserSession.getInstance().clearSession();int connections=f.connections;
  denied(()->DatabaseHandler.getAuditEvents(0));check(f.connections==connections,"Unauthenticated audit viewing must deny before JDBC");
  f.login("10002");connections=f.connections;denied(()->DatabaseHandler.getAuditEvents(0));check(f.connections==connections,"Employee audit viewing must deny before JDBC");
  f.login("10001");expectPolicy(()->DatabaseHandler.getAuditEvents(-1));
  List<AuditEvent> events=DatabaseHandler.getAuditEvents(0);
  check(events.stream().anyMatch(event->event.getAction().equals("ATTENDANCE_STATUS_CHANGED")&&event.getDetails().equals("status_before=Pending;status_after=Approved")),"Audit history must expose allowlisted attendance before/after status metadata");
  check(events.stream().anyMatch(event->event.getAction().equals("PAYROLL_STATUS_CHANGED")&&event.getDetails().equals("status_before=Pending;status_after=Verified")),"Audit history must expose allowlisted payroll before/after status metadata");
  check(events.stream().anyMatch(event->event.getAction().equals("AUTH_FAILURE"))&&events.stream().anyMatch(event->event.getAction().equals("USER_CREATED")||event.getAction().equals("LEGACY_LEDGERS_IMPORTED")||event.getAction().equals("ATTENDANCE_REQUEST_CREATED")),"Audit viewer must include authentication and business event types");
  int history=f.store.events.size();DatabaseHandler.getAuditEvents(0);check(f.store.events.size()==history,"Audit viewing must not mutate audit history");
  try{events.clear();throw new AssertionError("Audit pages must be immutable");}catch(UnsupportedOperationException expected){assertions++;}
  f.account("10001").role="Employee";denied(()->DatabaseHandler.getAuditEvents(0));check(EmpUserSession.getInstance().getCurrentUser()==null,"Audit viewing must revalidate the current database role");f.account("10001").role="HR";f.login("10001");
  history=f.store.events.size();f.failAudit=true;expectPersistence(()->CSVHandler.updatePayrollStatus("security-pay","10002","2027-02-01","2027-02-15","Verified",""));f.failAudit=false;
  check(f.store.events.size()==history&&"Pending".equals(CSVHandler.loadPayrollLedger().get(0)[21]),"Transition metadata and business status must roll back together on audit failure");
  expectPolicy(()->PersistenceSupport.inTransaction("reject invalid audit transition",connection->{AuthorizationService.requireHr(connection);AuditService.businessTransition(connection,"PAYROLL_STATUS_CHANGED","security-pay","Pending","UNSUPPORTED_STATUS");}));
  PersistenceSupport.inTransaction("seed test-only paging history",connection->{AuthorizationService.requireHr(connection);for(int i=0;i<105;i++){AuditService.business(connection,"TEST_HISTORY","offline_"+i);}});
  List<AuditEvent> newest=DatabaseHandler.getAuditEvents(0);check(newest.size()==100,"Audit history must bound a page to100 events");
  boolean descending=true;for(int i=1;i<newest.size();i++){descending&=newest.get(i-1).getEventId()>newest.get(i).getEventId();}check(descending,"Audit history must order newest first");
  long cursor=newest.get(newest.size()-1).getEventId();List<AuditEvent> older=DatabaseHandler.getAuditEvents(cursor);
  check(!older.isEmpty()&&older.stream().allMatch(event->event.getEventId()<cursor),"Older pagination must exclude the cursor and every newer event");
  check(newest.size()+older.size()==f.store.events.size(),"Keyset paging must reach the complete test history without losing events");
  check(older.stream().allMatch(event->event.getOccurredAt()!=null&&event.getActor()!=null&&event.getOutcome()!=null&&event.getSourceMachine()!=null),"Audit DTOs must expose the agreed metadata fields");
 }
 private static void auditIntegrity(BackendJdbcFixture f) {
  check(DatabaseHandler.verifyAuditChain(),"Supported operations must retain a valid full chain");
  String vector=AuditService.eventHash("0".repeat(64),1,Instant.parse("2026-10-17T00:00:00Z"),"actor","ACT","target","SUCCESS","machine","details");
  check(vector.equals("52821b0f48f7f880591aabd50afaf166768455516d4a9a6d692a7bae9e34e4fe"),"Audit canonicalization must match an independent fixed SHA256 vector");
  check(!AuditService.eventHash("0".repeat(64),1,Instant.parse("2026-10-17T00:00:00Z"),"a|b","c").equals(AuditService.eventHash("0".repeat(64),1,Instant.parse("2026-10-17T00:00:00Z"),"a","b|c")),"Delimited values must not collide across audit fields");
  Map<String,Object> event=f.store.events.get(0);Object original=event.put("Details","tampered");check(!DatabaseHandler.verifyAuditChain(),"Editing a stored event must fail chain verification");f.store.events.get(0).put("Details",original);
  Map<String,Object> last=f.store.events.remove(f.store.events.size()-1);check(!DatabaseHandler.verifyAuditChain(),"Deleting a tail event must fail against the persisted head");f.store.events.add(last);
  long head=f.store.headId;String hash=f.store.headHash;f.store.headId=0;f.store.headHash="0".repeat(64);check(!DatabaseHandler.verifyAuditChain(),"Rolling back the head must not hide an orphan audit tail");f.store.headId=head;f.store.headHash=hash;
  check(DatabaseHandler.verifyAuditChain(),"Restoring the original data must restore chain agreement");
  check(f.store.events.stream().map(Object::toString).allMatch(text->!text.contains(new String(PERSONAL))&&!text.contains(new String(TEMPORARY))&&!text.contains(f.account("10001").hash)),"Every audit event must exclude raw credentials and stored password hashes");
 }
 private static void check(boolean value,String message){assertions++;if(!value){throw new AssertionError(message);}}
 private static void expectPolicy(Runnable action){try{action.run();}catch(IllegalArgumentException expected){assertions++;return;}throw new AssertionError("Expected policy rejection");}
 private static void expectPersistence(Runnable action){try{action.run();}catch(PersistenceException expected){assertions++;return;}throw new AssertionError("Expected safe persistence failure");}
 private static void denied(Runnable action){try{action.run();}catch(AuthorizationException expected){assertions++;return;}throw new AssertionError("Expected authorization denial");}
 private static AuthenticationThrottledException throttled(Runnable action){try{action.run();}catch(AuthenticationThrottledException expected){assertions++;return expected;}throw new AssertionError("Expected persisted retry-after rejection");}
}

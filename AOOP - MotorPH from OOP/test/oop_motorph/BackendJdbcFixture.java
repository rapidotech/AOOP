package oop_motorph;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/** Test-only transactional JDBC double. No production bypass or real JDBC driver is used. */
final class BackendJdbcFixture implements AutoCloseable, Driver {
    static final String URL="jdbc:mysql://backend-test.invalid/aoop";
    static final char[] PASSWORD="Offline-personal-proof!".toCharArray();
    Store store=new Store();
    Instant now=Instant.parse("2026-10-17T00:00:00Z");
    boolean failAudit, failWrite;
    int departmentRows=1, commits, rollbacks, connections;
    final List<String> statements=new ArrayList<>();
    Properties properties;
    private final Map<String,String> previous=new HashMap<>();

    BackendJdbcFixture() throws Exception {
        String hash=PasswordService.hashPassword(PASSWORD);
        user(1,"10001","HR",hash,false); user(2,"10002","Employee",hash,false); user(3,"10003","Employee",hash,false);
        for(String key:List.of("aoop.db.url","aoop.db.user","aoop.db.password","aoop.db.config")){previous.put(key,System.getProperty(key));}
        System.setProperty("aoop.db.url",URL);System.setProperty("aoop.db.user","offline");System.setProperty("aoop.db.password","offline-only");
        System.setProperty("aoop.db.config",System.getProperty("java.io.tmpdir")+"/motorph-no-real-config.properties");
        DriverManager.registerDriver(this);
        EmpUserSession.getInstance().clearSession();
    }
    void user(int id,String name,String role,String hash,boolean change){
        User user=new User();user.id=id;user.name=name;user.role=role;user.hash=hash;user.change=change;store.users.put(id,user);
        store.profiles.put(name,profile(name,"First","Last"));
    }
    DatabaseHandler.AuthenticationResult login(String username){
        EmpUserSession.getInstance().clearSession();
        DatabaseHandler.AuthenticationResult result=DatabaseHandler.authenticate(username,PASSWORD);
        if(result!=null&&!result.isResetRequired()){EmpUserSession.getInstance().establish(result);}
        return result;
    }
    void advance(long seconds){now=now.plusSeconds(seconds);}
    void clearThrottle(){for(User u:store.users.values()){u.failures=0;u.retry=null;u.lock=null;}store.sources.clear();}
    User account(String name){return store.users.values().stream().filter(u->u.name.equals(name)).findFirst().orElseThrow();}
    public void close()throws Exception{
        EmpUserSession.getInstance().clearSession();DriverManager.deregisterDriver(this);
        for(Map.Entry<String,String> entry:previous.entrySet()){if(entry.getValue()==null){System.clearProperty(entry.getKey());}else{System.setProperty(entry.getKey(),entry.getValue());}}
    }

    public Connection connect(String url,Properties input){
        if(!acceptsURL(url)){return null;}connections++;properties=input;Store[] working={store.copy()};boolean[] closed={false};boolean[] auto={true};
        return proxy(Connection.class,(method,args)->{
            switch(method){
                case "setAutoCommit":auto[0]=(Boolean)args[0];return null;
                case "getAutoCommit":return auto[0];
                case "prepareStatement":statements.add((String)args[0]);return statement(working,(String)args[0]);
                case "commit":store=working[0].copy();commits++;return null;
                case "rollback":working[0]=store.copy();rollbacks++;return null;
                case "close":closed[0]=true;return null;
                case "isClosed":return closed[0];
                default:return null;
            }
        });
    }

    private PreparedStatement statement(Store[] state,String sql){
        Map<Integer,Object> parameters=new HashMap<>();
        return proxy(PreparedStatement.class,(method,args)->{
            if(method.startsWith("set")&&args!=null&&args.length>=2){parameters.put((Integer)args[0],method.equals("setNull")?null:args[1]);return null;}
            if(method.equals("executeQuery")){return result(query(state[0],sql,parameters));}
            if(method.equals("executeUpdate")){return update(state[0],sql,parameters);}
            if(method.equals("execute")){update(state[0],sql,parameters);return false;}
            return null;
        });
    }

    private int update(Store s,String sql,Map<Integer,Object> p)throws SQLException{
        String compact=sql.replace(" ","").toLowerCase(Locale.ROOT);
        if(sql.startsWith("CALL motorph_append_audit")){
            if(failAudit){throw new SQLException("synthetic audit failure");}
            long id=s.headId+1;String previous=s.headHash;
            String[] fields=new String[6];for(int i=0;i<6;i++){fields[i]=(String)p.get(i+1);}
            Map<String,Object> event=row("EventID",id,"OccurredAtUTC",Timestamp.from(now),"ActorID",fields[0],"Action",fields[1],"TargetID",fields[2],"Outcome",fields[3],"SourceMachine",fields[4],"Details",fields[5],"PreviousHash",previous,"EntryHash",AuditService.eventHash(previous,id,now,fields));
            s.events.add(event);s.headId=id;s.headHash=(String)event.get("EntryHash");return 1;
        }
        if(failWrite){throw new SQLException("synthetic write failure");}
        if(sql.startsWith("INSERT IGNORE INTO AuthThrottle")){s.sources.putIfAbsent((String)p.get(1),new Source());return 1;}
        if(sql.startsWith("UPDATE AuthThrottle")){Source source=s.sources.get(p.get(4));source.failures=(Integer)p.get(1);source.retry=instant(p.get(2));source.lock=instant(p.get(3));return 1;}
        if(compact.startsWith("updateuserssetfailed_attempts=")){User user=s.users.get(p.get(4));if(user==null){return 0;}user.failures=(Integer)p.get(1);user.retry=instant(p.get(2));user.lock=instant(p.get(3));return 1;}
        if(compact.startsWith("updateuserssetpassword=")){User user=s.users.get(p.get(2));if(user==null||!user.change||!user.hash.equals(p.get(3))){return 0;}user.hash=(String)p.get(1);user.change=false;user.failures=0;user.retry=null;user.lock=null;return 1;}
        if(sql.startsWith("INSERT INTO users")){int id=s.users.keySet().stream().mapToInt(Integer::intValue).max().orElse(0)+1;User user=new User();user.id=id;user.name=(String)p.get(1);user.hash=(String)p.get(2);user.role=(String)p.get(3);user.change=true;s.users.put(id,user);return 1;}
        if(sql.startsWith("INSERT INTO employees")){String id=String.valueOf(p.get(1));if(s.profiles.containsKey(id)){throw new SQLException("duplicate fixture employee");}s.profiles.put(id,profile(id,(String)p.get(2),(String)p.get(3)));return 1;}
        if(sql.startsWith("UPDATE employees")){Object id=p.get(p.size());return s.profiles.containsKey(String.valueOf(id))?1:0;}
        if(sql.startsWith("DELETE FROM employees")){return s.profiles.remove(String.valueOf(p.get(1)))==null?0:1;}
        if(sql.startsWith("INSERT INTO AttendanceRequests")||sql.startsWith("INSERT INTO PayrollLedger")){
            boolean attendance=sql.contains("AttendanceRequests");Map<Long,String[]> rows=attendance?s.attendance:s.payroll;String[] values=payload(p,attendance);
            for(String[] old:rows.values()){if(sameKey(old,values,attendance)||(!attendance&&old[0].equals(values[0]))){throw new SQLException("duplicate ledger key");}}
            rows.put(s.nextRecord++,values);return 1;
        }
        if(sql.startsWith("UPDATE AttendanceRequests")||sql.startsWith("UPDATE PayrollLedger")){
            boolean attendance=sql.contains("AttendanceRequests");Map<Long,String[]> rows=attendance?s.attendance:s.payroll;long id=((Number)p.get(p.size())).longValue();
            if(!rows.containsKey(id)){return 0;}String[] values=payload(p,attendance);
            for(Map.Entry<Long,String[]> old:rows.entrySet()){if(old.getKey()!=id&&sameKey(old.getValue(),values,attendance)){throw new SQLException("duplicate ledger key");}}
            rows.put(id,values);return 1;
        }
        if(sql.startsWith("DELETE FROM PayrollLedger")){if(p.isEmpty()){s.payroll.clear();}else{s.payroll.values().removeIf(r->r[1].equals(String.valueOf(p.get(1))));}return 1;}
        if(sql.startsWith("DELETE FROM AttendanceRequests")){s.attendance.values().removeIf(r->r[0].equals(String.valueOf(p.get(1))));return 1;}
        return 1;
    }

    private List<Map<String,Object>> query(Store s,String sql,Map<Integer,Object> p){
        if(sql.contains("FROM AuthThrottle")){Source source=s.sources.get(p.get(1));return source==null?List.of():List.of(row("FailedAttempts",source.failures,"RetryAfter",timestamp(source.retry),"LockedUntil",timestamp(source.lock),"ServerNow",Timestamp.from(now)));}
        if(sql.contains("FROM users WHERE")){
            User user=sql.contains("UserID = ?")?s.users.get(p.get(1)):s.users.values().stream().filter(u->u.name.equals(p.get(1))).findFirst().orElse(null);
            return user==null?List.of():List.of(row("UserID",user.id,"username",user.name,"password",user.hash,"role",user.role,"must_change_password",user.change,"failed_attempts",user.failures,"next_login_at",timestamp(user.retry),"locked_until",timestamp(user.lock)));
        }
        if(sql.contains("FROM AuditChainHead")){return List.of(row("LastEventID",s.headId,"LastHash",s.headHash));}
        if(sql.contains("FROM AuditLog")){
            List<Map<String,Object>> rows=new ArrayList<>();
            for(Map<String,Object> event:s.events){
                long id=(Long)event.get("EventID");
                boolean included=!sql.contains("Action IN")||(id>((Number)p.get(1)).longValue()&&Set.of("AUTH_FAILURE","AUTH_LOCKED").contains(event.get("Action")));
                if(sql.contains("EventID < ?")){included&=id<((Number)p.get(1)).longValue();}
                if(included){rows.add(event);}
            }
            if(sql.contains("DESC")){Collections.reverse(rows);}
            if(sql.contains("LIMIT 100")&&rows.size()>100){return new ArrayList<>(rows.subList(0,100));}
            return rows;
        }
        if(sql.contains("FROM AttendanceRequests")||sql.contains("FROM PayrollLedger")){
            boolean attendance=sql.contains("AttendanceRequests");Map<Long,String[]> records=attendance?s.attendance:s.payroll;List<Map<String,Object>> rows=new ArrayList<>();
            for(Map.Entry<Long,String[]> record:records.entrySet()){
                String[] v=record.getValue();int parameter=1;boolean matches=true;
                if(sql.contains("EmployeeID=?")){matches&=v[attendance?0:1].equals(String.valueOf(p.get(parameter++)));}
                if(sql.contains("DateFrom=?")){matches&=DatabaseLedger.date(v[attendance?6:4]).equals(((java.sql.Date)p.get(parameter++)).toLocalDate());}
                if(sql.contains("DateTo=?")){matches&=DatabaseLedger.date(v[attendance?7:5]).equals(((java.sql.Date)p.get(parameter++)).toLocalDate());}
                if(sql.contains("TransactionID=?")){matches&=v[0].equals(p.get(parameter));}
                if(matches){Map<String,Object> row=row("RecordID",record.getKey(),"Status",v[attendance?13:21]);for(int i=0;i<v.length;i++){row.put("C"+i,v[i]);}rows.add(row);}
            }return rows;
        }
        if(sql.startsWith("SELECT DISTINCT DepartmentID")){List<Map<String,Object>> rows=new ArrayList<>();for(int i=0;i<departmentRows;i++){rows.add(row("DepartmentID",10+i));}return rows;}
        if(sql.startsWith("SELECT AddressID")){return List.of(row("AddressID",1));}
        if(sql.startsWith("SELECT StatusID")){return List.of(row("StatusID",1));}
        if(sql.startsWith("SELECT PositionID")){return List.of(row("PositionID",1));}
        if(sql.startsWith("SELECT MAX")){return List.of(row("MAX(EmployeeID)",s.profiles.keySet().stream().mapToInt(Integer::parseInt).max().orElse(0)));}
        if(sql.startsWith("SELECT COUNT")){String employee=String.valueOf(p.get(1));return List.of(row("count",s.profiles.containsKey(employee)&&!employee.equals(p.get(2))?1:0));}
        if(sql.contains("FROM employees e")){
            List<Map<String,Object>> rows=new ArrayList<>();for(Map.Entry<String,Map<String,Object>> profile:s.profiles.entrySet()){
                boolean filtered=sql.contains("EmployeeID = ?");if(!filtered||profile.getKey().equals(p.get(1))){rows.add(profile.getValue());}
            }return rows;
        }
        return List.of();
    }

    private ResultSet result(List<Map<String,Object>> rows){
        int[] cursor={-1};boolean[] wasNull={false};
        return proxy(ResultSet.class,(method,args)->{
            if(method.equals("next")){return ++cursor[0]<rows.size();}
            if(method.equals("wasNull")){return wasNull[0];}
            if(method.equals("getMetaData")){return proxy(ResultSetMetaData.class,(name,arguments)->name.equals("getColumnCount")?0:null);}
            if(method.startsWith("get")&&args!=null&&args.length>0){
                Map<String,Object> row=rows.get(cursor[0]);Object value;
                if(args[0] instanceof Number){int index=((Number)args[0]).intValue()-1;value=index<row.size()?new ArrayList<>(row.values()).get(index):null;}
                else{String key=String.valueOf(args[0]);value=null;for(Map.Entry<String,Object> entry:row.entrySet()){if(entry.getKey().equalsIgnoreCase(key)){value=entry.getValue();break;}}}
                wasNull[0]=value==null;
                switch(method){
                    case "getString":return value==null?null:String.valueOf(value);
                    case "getBoolean":return value instanceof Boolean?value:value instanceof Number&&((Number)value).intValue()!=0;
                    case "getInt":return value instanceof Number?((Number)value).intValue():0;
                    case "getLong":return value instanceof Number?((Number)value).longValue():0L;
                    case "getDouble":return value instanceof Number?((Number)value).doubleValue():0.0;
                    default:return value;
                }
            }return null;
        });
    }

    private static String[] payload(Map<Integer,Object> p,boolean attendance){int count=attendance?20:22;int start=attendance?5:6;String[] row=new String[count];for(int i=0;i<count;i++){row[i]=(String)p.get(start+i);}return row;}
    private static boolean sameKey(String[] a,String[] b,boolean attendance){return a[attendance?0:1].equals(b[attendance?0:1])&&DatabaseLedger.date(a[attendance?6:4]).equals(DatabaseLedger.date(b[attendance?6:4]))&&DatabaseLedger.date(a[attendance?7:5]).equals(DatabaseLedger.date(b[attendance?7:5]));}
    static Map<String,Object> row(Object...pairs){Map<String,Object> row=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2){row.put((String)pairs[i],pairs[i+1]);}return row;}
    private static Map<String,Object> profile(String id,String first,String last){return row("EmployeeID",id,"FirstName",first,"LastName",last,"Birthday","1990-01-01","StreetAddress","Address","City","City","Region","Region","PhoneNumber","1234567890","StatusName","Regular","PositionName","Engineer","SupervisorName","N/A");}
    private static Instant instant(Object value){return value instanceof Timestamp?((Timestamp)value).toInstant():null;}
    private static Timestamp timestamp(Instant value){return value==null?null:Timestamp.from(value);}
    private interface Handler{Object call(String method,Object[] arguments)throws Exception;}
    @SuppressWarnings("unchecked") private static <T>T proxy(Class<T> type,Handler handler){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(object,method,args)->{
        Object value=handler.call(method.getName(),args);if(value!=null||!method.getReturnType().isPrimitive()||method.getReturnType()==void.class){return value;}
        if(method.getReturnType()==boolean.class){return false;}if(method.getReturnType()==long.class){return 0L;}if(method.getReturnType()==double.class){return 0.0;}return 0;
    });}
    public boolean acceptsURL(String url){return URL.equals(url);}public DriverPropertyInfo[]getPropertyInfo(String url,Properties properties){return new DriverPropertyInfo[0];}
    public int getMajorVersion(){return 1;}public int getMinorVersion(){return 0;}public boolean jdbcCompliant(){return false;}public Logger getParentLogger(){return Logger.getGlobal();}
    static final class User{int id,failures;String name,role,hash;boolean change;Instant retry,lock;User copy(){User u=new User();u.id=id;u.failures=failures;u.name=name;u.role=role;u.hash=hash;u.change=change;u.retry=retry;u.lock=lock;return u;}}
    static final class Source{int failures;Instant retry,lock;Source copy(){Source s=new Source();s.failures=failures;s.retry=retry;s.lock=lock;return s;}}
    static final class Store{
        final Map<Integer,User>users=new LinkedHashMap<>();final Map<String,Source>sources=new HashMap<>();final Map<String,Map<String,Object>>profiles=new LinkedHashMap<>();
        final Map<Long,String[]>attendance=new LinkedHashMap<>(),payroll=new LinkedHashMap<>();final List<Map<String,Object>>events=new ArrayList<>();long nextRecord=1,headId=0;String headHash="0".repeat(64);
        Store copy(){Store s=new Store();users.forEach((k,v)->s.users.put(k,v.copy()));sources.forEach((k,v)->s.sources.put(k,v.copy()));profiles.forEach((k,v)->s.profiles.put(k,new LinkedHashMap<>(v)));
            attendance.forEach((k,v)->s.attendance.put(k,v.clone()));payroll.forEach((k,v)->s.payroll.put(k,v.clone()));for(Map<String,Object> e:events){s.events.add(new LinkedHashMap<>(e));}s.nextRecord=nextRecord;s.headId=headId;s.headHash=headHash;return s;}
    }
}

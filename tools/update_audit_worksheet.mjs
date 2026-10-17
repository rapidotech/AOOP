import fs from 'node:fs/promises';
import path from 'node:path';
import { createRequire } from 'node:module';
import { pathToFileURL } from 'node:url';
const home=process.env.USERPROFILE;
const work=path.join(home,'.codex/tmp/astra-runs/aoop-20261017-all-controls/workbook-builder');
const require=createRequire(pathToFileURL(path.join(work,'package.json')));
const {FileBlob,SpreadsheetFile}=await import(pathToFileURL(require.resolve('@oai/artifact-tool')).href);
const original=path.join(home,'Downloads/MO-IT153 _ Security Controls Audit Worksheet _ S4101 _ MotorPH_ Payroll System _ Group 13 (1).xlsx');
const output=path.join(home,'Downloads/AOOP_Milestone2_Group13/outputs/all-controls');
await fs.mkdir(output,{recursive:true});
const workbook=await SpreadsheetFile.importXlsx(await FileBlob.load(original));
const sheet=workbook.worksheets.getItem('Audit Worksheet');
if(process.argv.includes('--inspect')){
 const preview=await workbook.render({sheetName:'Audit Worksheet',range:'E73:I78',scale:1,format:'png'});
 await fs.writeFile(path.join(work,'original-controls.png'),new Uint8Array(await preview.arrayBuffer()));
 console.log((await workbook.inspect({kind:'table',range:"'Audit Worksheet'!E73:I78",include:'values,formulas',tableMaxRows:6,tableMaxCols:5,maxChars:1800})).ndjson);
 process.exit(0);
}
// Text-only revision; preserve the supplied workbook's sheets, objects and formatting.
const integration=JSON.parse(await fs.readFile(path.join(home,'Downloads/AOOP_Milestone2_Group13/evidence/expanded-controls-integration.json'),'utf8'));
const proof=`${integration.runtime.assertions} real application checks and ${integration.sql_checks} SQL/chain checks passed in aoop_m2:3307.`;
const updates={
 'B3':'Security Controls Reassessment',
 'E11':'MotorPH Java Swing payroll application. This revised working copy records the expanded Milestone 2 controls verified in an isolated local clone on 17 October 2026. Employee and HR workflows remain available. The original Milestone 1 audit workbook and original aoop database were preserved unchanged.',
 'E12':'Two-tier Swing/JDBC architecture. Java17 portable UTF-8 build. MySQL8.0.42 clone at127.0.0.1:3307/aoop_m2. Windows DPAPI protected credential profile, verified TLS identity, restricted application grants. BCrypt12 plus mandatory reset, persisted throttles, verified one-use session capabilities and scoped DAL. Attendance requests/payroll workflow now use transactional database ledgers. Server-serialized audit chain and verified local archive task added.',
 'E13':'IN SCOPE: supported-client authentication/authorization, isolated clone database grants/transport, transactional audit and workflow ledgers, source/build/dependency guards. OUT OF SCOPE: mutation of original aoop/shared root, public internet or production effectiveness, administrator-resistant custody, changed repository visibility/history, physical printing and statutory payroll correctness. Private R2 off-host copies are now verified; email alerts and expanded roles remain deferred.',
 'E15':'Executed: Java17 offline regression runner; real isolated JDBC/native SQL assurance; Gitleaks8.30.1 with custom low-entropy DatabaseConfig rule; SHA256 dependency checks; live OSV Maven package/version advisory queries. Semgrep/Dependency-Check were not re-executed successfully in this Windows run; WSL Ubuntu failed with E_UNEXPECTED. OSV is the documented working advisory alternative, not an NVD-completeness claim.',
 'E16':'Manual review of actual diff, permission matrix, constructor order, transaction boundaries and audit procedure. Disposable real HR/employee fixtures tested allowed/denied operations, lockout and reset throttling. Audit EXECUTE privilege was temporarily revoked on the clone to prove business rollback, then restored. Eight concurrent appends and altered archive copies tested chain verification. UI evidence uses injected fixtures and is labeled separately.',
 'E17':'Authentication: persisted state and cross-JVM rejection. Authorization: direct DAL/self-vs-other and constructor tests. Audit: same-transaction append/rollback and no direct audit DML. Transport: loopback listener, TLS profile and plaintext denial. Repository: custom secret detector and redacted Gitleaks. Dependencies: four pinned JARs, live OSV. Build: Java17/UTF-8. See the implementation log and sanitized evidence files.',
 'E19':'Original IAS1 risks were exposed DB credentials, weak/default passwords, unrestricted guessing, UI-only authorization and absent audit. The clone now has verified local controls for each. Remaining risks are the unchanged original shared credential/history, a modified client holding the shared JDBC account, spoofable source labels, absent independent archive custody and unverified production exposure.',
 'E20':'C1: DPAPI profile, restricted clone account, credential rotation, custom Gitleaks hook/CI. C2: BCrypt12, unique bootstrap credentials and mandatory reset; legitimate plaintext-verified transparent rehash is superseded for these remediated default accounts. SHA-digest wrapping is rejected. Password-column encryption with a desktop-held key is deferred due key-management cost and limited extra protection. C3: persistent account/source lockout, retry deadlines, authentication audit and desktop HR notification; email remains deferred. HikariCP is deferred until a measured connection bottleneck exists. C4: current HR/Employee DAL/object/constructor guards; expanded roles deferred. C5: same-transaction audit, private serialized hash-chain append, allowlisted status-before/status-after metadata, read-only paginated HR business viewer and local verified archives plus private same-PC Docker S3 with retained versions. Full old/new personnel/password values are excluded; R2 off-host recovery is verified; independently administered custody is not proven.',
 'E21':'Users are internal Employee/HR roles. The supported desktop is a two-tier JDBC client; its shared credential does not prove a human identity to the DB, and source-machine names are telemetry. Testing uses isolated aoop_m2:3307 and disposable fixtures, including intentional controlled mutations. Original aoop:3306, original credentials/service, source documents, Git history and repository visibility are preserved.',
 'E23':'Current assessment covers all five controls, including HR read-only business audit pagination and allowlisted status changes, plus transport, repository/build/dependency and release checks. Original audit controls and proposed alternatives are mapped to implemented, superseded, deferred or partial decisions in CONTROL_ASSESSMENT.md and the expanded Implementation Log.',
 'E24':'Priority1: preserve the completed credential/password remediation and coordinate original root/history/privacy rollout. Priority2: maintain scoped authorization, transactional audit and workflow validation in the supported client. Priority3: establish independent archive custody and trusted server-side human authentication for stronger guarantees. Keep lockout, release/source/advisory gates and generic failure UX from regressing.',
 'E28':'Executed Gitleaks8.30.1 custom-rule current-source scan, focused credential guard, positive/negative detector regression and private source-literal comparison. Historical Semgrep findings remain in the original workbook; no new Semgrep pass is claimed.',
 'E29':'Inspected protected launch, actual clone account/URL, table grants, DPAPI file handling and current source. Tested missing/corrupt configuration and unauthorized SQL. Scope is the isolated clone.',
 'E30':'C1 protected profile and grants remain verified; expanded-migration.json, expanded-controls-integration.json, gitleaks-current.json and final-source-check.json. Original exposed credential/history remain a coordinated rollout limitation.',
 'E31':'PASS IN ISOLATED CLONE',
 'E32':'Protected DPAPI launch uses a restricted local application account and verified TLS. Current-source guard and commit hook prevent the previously missed constant pattern. No shared-original root rotation or history rewrite was performed.',
 'E34':'Java regression suite and real isolated JDBC authentication/reset tests using BCrypt cost12. No new Semgrep execution is claimed.',
 'E35':'Reviewed PasswordService, AuthenticationService and reset transaction. Original E02 is retained as baseline evidence; disposable accounts prove replacement storage, flag clearing and old-credential rejection.',
 'E36':'C2-controlled-enrollment.json and C2-escrow-atomic-maintenance.json retain original enrollment evidence. Expanded runtime checks reconfirm reset restriction, credential replacement, old temporary proof rejection and reset throttling.',
 'E37':'PASS IN ISOLATED CLONE',
 'E38':'All34 original cloned accounts were enrolled with unique BCrypt12 temporary credentials and remain change-required. A disposable account proves reset completion; no hashes or plaintext credentials are in public evidence.',
 'E40':'Real isolated Java/JDBC fixture suite and SQL counter/deadline observations. Authentication failures commit before exceptions; no long test sleeps or EDT sleeps.',
 'E41':'Tested5failures, persisted deadlines, correct password during lock, password-reset proof throttling and fresh-JVM rejection after changing the source label. Test administrator expires deadlines between independent cases; duration is checked directly.',
 'E42':'expanded-controls-integration.json:5failures,1/2/4/8-second retry times,15-minute lock, correct-password rejection during lock, reset-proof throttling and fresh-JVM/source-change rejection. Admin activity UI proof is separate.',
 'E43':'PASS IN SUPPORTED CLIENT',
 'E44':'State and failure audit events commit before rejection is reported. Test-admin deadline expiration accelerates independent scenarios; the real15-minute duration is checked in SQL. Source-machine labels are telemetry, not a trusted device identity.',
 'E46':'Offline injected UI tests plus real isolated direct-DAL calls. Tests invoke sensitive methods without relying on hidden/disabled buttons.',
 'E47':'Inspected all restricted constructors and sensitive data paths. Used genuine successful authentication results, own/other employee IDs, changed reset state and unauthorized HR operations. Bulk credential dump entry point is denied.',
 'E48':'expanded-controls-integration.json and offline UI suite: unauthenticated DAL rejection, employee self scope, denied HR writes/activity/import, changed-account invalidation and constructor guards before data loading.',
 'E49':'PASS IN SUPPORTED CLIENT',
 'E50':'AuthorizationService validates a committed authentication capability and current database role/reset/credential state. Shared direct JDBC credentials still allow a modified client to issue permitted SQL; this is not a server-side human-identity boundary.',
 'E52':'Native SQL and real JDBC tests, independent Python archive verification, optimized-mode negative integrity checks and two bounded UI evidence rounds.',
 'E53':'Temporarily revoked audit EXECUTE privilege to force business rollback, then restored it. Tested concurrent serialized appends, denied direct audit DML, malformed archive copies, exact legacy reimport/conflict handling and workflow approvals. Source CSV inventory found no existing external ledgers; source files are preserved.',
 'E54':`expanded-controls-integration.json:${integration.sql_checks}SQL/chain checks plus runtime mutation/audit rollback, import idempotence/conflict, approval and HR business-audit tests. archive_audit.py verifies full sequence/hash/head using a consistent snapshot. Daily dual-destination task ran successfully; cloudflare-r2-assurance.json records 20 live R2 checks and eight rejected operations.`,
 'E55':'PASS: TESTED LOCAL AND OFF-HOST SCOPE',
 'E56':'Business mutations and audit append share a transaction. Workflow CSV APIs now use database ledgers; local write bypass is closed. Audit table/head DML is denied to the app. Local and R2 off-host archive recovery are verified. R2 administrators can remove locks; independent administration is not proven.',
 'E58':'Native loopback listener inspection, protected launcher identity/TLS probe and isolated JDBC plaintext-denial test. No new Nmap scan of another host/network is claimed.',
 'E59':'Checked actual3307clone identity, secure transport requirement, negotiated TLS, verified certificate hostname and application grants. Original3306service is unchanged.',
 'E60':'Protected launcher identity/TLS probe and expanded runtime denials on the owned3307clone. Native listener inspection records loopback binding; no original3306orhostfirewall change is claimed.',
 'E61':'PASS IN ISOLATED CLONE',
 'E62':'Clone requires secure transport and validates server identity. This proves local clone posture only; it does not establish production or LAN-wide exposure.',
 'E64':'Java17 UTF-8 build/offline regression suite, redacted Gitleaks8.30.1 current-source scan, dependency SHA256 inventory and liveOSV querybatch. NVD/Dependency-Check and Semgrep were not successfully re-executed in this run.',
 'E65':'Reviewed source/build entry points, pinned JAR provenance, commit hook, authored source CI and release command. Prior generated output was untracked while local files were retained; original Git history remains unchanged.',
 'E66':'Offline verification output, gitleaks-current.json, dependency-advisory-baseline.json, dependency-advisory-current.json and pinned dependency inventory. Connector/J8.0.33 advisoryGHSA-m6vm-37g8-gqvh led to verified9.7.0 replacement.',
 'E67':'PARTIAL: LOCAL GATES VERIFIED',
 'E68':'UTF-8/Java17 and pinned checksummed libraries verified. Live OSV returned no advisories for current versions, not a guarantee of no vulnerabilities. GitHub CI run38997465784 passed 352 offline assertions, source secret scanning and dependency checks on17October2026. Original Git history/publicness remain unchanged.',
 'G73':'DPAPI profile, clone-only credential rotation, least-privilege grants, TLS identity and custom secret scan verified.', 'H73':'Yes, isolated scope', 'I73':'Keep. Coordinate original credential rotation and history/privacy separately.',
 'G74':'BCrypt12, unique bootstrap credentials and mandatory reset verified; reset proof is rate-limited.', 'H74':'Yes, isolated scope', 'I74':'Keep. Preserve protected enrollment escrow and reset instructions.',
 'G75':'Persistent account/source throttling, failure audit and HR desktop notification implemented. '+proof, 'H75':'Yes, supported client', 'I75':'Keep. Source label is spoofable telemetry. Email remains deferred.',
 'G76':'Central DAL/object guards, authenticated capability, database state recheck and constructor checks implemented.', 'H76':'Yes, supported client', 'I76':'Keep current Employee/HR roles. A trusted server/API is needed against a modified JDBC client.',
 'G77':'Transactional chain, protected append, DB workflow ledgers and HR audit verified. Local RustFS passed 18 checks; new Cloudflare R2 off-host copies passed 20 live checks and scheduled recovery verification. Seven-day R2 bucket locks block deletion/overwrite while active; eight-day expiry configured. Conservative storage/request limits protect intended free-tier use.', 'H77':'Verified', 'I77':'Keep. R2 administrators can remove locks; independent administration is not proven. R2 is metered, without a guaranteed $0 account cap.',
 'G78':'Clone loopback/TLS enforced. Connector/J upgraded to9.7.0; four pinned JARs checked against liveOSV.', 'H78':'Yes, isolated scope', 'I78':'Keep. Do not infer original-service hardening or complete vulnerability absence.',
 'E79':'The expanded implementation closes supported-client gaps in lockout, DAL authorization and transactional audit coverage. Tests deliberately trigger denials and audit failure, rather than inferring effectiveness from scanners. Hash chaining and local archives do not establish independent custody. A modified desktop client holding the shared JDBC account remains a wider architectural boundary.',
 'E81':'Keep all five controls and network/build/repository/release guards. C1/C2 remain verified; C3/C4 and local C5 mechanisms now have actual runtime proof. Preserve PreparedStatement use and business transactions. Evidence: expanded-controls-integration.json and current implementation log.',
 'E82':'Revisions implemented: nonblocking persisted retry deadlines; genuine one-use authentication capability; self-scoped DAL; server-serialized append procedure; DB workflow ledgers with validated legacy import; custom secret regression; liveOSV advisory checks. Actual R2 off-host recovery is now verified; do not claim independent administration, history rewriting or shared-root rotation complete.',
 'E83':'Supporting controls now include the loopback/TLS clone, local commit hook, source CI definition, pinned checksummed libraries, advisory feed gate, complete local release command and daily verified audit export. Remaining coordinated tasks are original-root/history/privacy remediation and stronger independently administered custody. Email and expanded roles remain deferred.'
};
for(const [cell,value] of Object.entries(updates)){
 let text=value;
 if(/^H7[3-8]$/.test(cell))text=value.startsWith('Partial')?'Partially':'Yes';
 if(/^E(31|37|43|49|55|61|67)$/.test(cell))text=value.startsWith('PARTIAL')?'PARTIAL':'PASS';
 sheet.getRange(cell).values=[[text]];
}
// The longer observed C5 result must remain fully visible in the template row.
sheet.getRange('E77:I77').format.rowHeight=155;
workbook.recalculate();
console.log((await workbook.inspect({kind:'table',range:"'Audit Worksheet'!G73:I78",include:'values',tableMaxRows:6,tableMaxCols:3,tableMaxCellChars:100,maxChars:2200})).ndjson);
const errors=await workbook.inspect({kind:'match',searchTerm:'#REF!|#DIV/0!|#VALUE!|#NAME\\?|#NUM!|#NULL!|#SPILL!|#CALC!',options:{useRegex:true,maxResults:30},summary:'Revised audit formula errors'});
await fs.writeFile(path.join(work,'worksheet-error-scan.txt'),errors.ndjson);
for(const [name,range] of [['controls','E73:I78'],['scope','B5:I17'],['findings','C27:I38']]){
 const preview=await workbook.render({sheetName:'Audit Worksheet',range,scale:1,format:'png'});
 await fs.writeFile(path.join(work,'updated-'+name+'.png'),new Uint8Array(await preview.arrayBuffer()));
}
await (await SpreadsheetFile.exportXlsx(workbook)).save(path.join(output,'MotorPH_Group13_Updated_Control_Audit.xlsx'));
console.log('Revised worksheet exported; original unchanged.');

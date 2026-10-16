-- Isolated Milestone 2 clone only. Administrator migration; no shared 3306 writes.
-- Execute with the MySQL client (DELIMITER is a client directive).
SET @motorph_has_column = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='failed_attempts');
SET @motorph_sql = IF(@motorph_has_column=0, 'ALTER TABLE users ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE motorph_expand FROM @motorph_sql;
EXECUTE motorph_expand;
DEALLOCATE PREPARE motorph_expand;
SET @motorph_has_column = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='locked_until');
SET @motorph_sql = IF(@motorph_has_column=0, 'ALTER TABLE users ADD COLUMN locked_until DATETIME(6) NULL', 'SELECT 1');
PREPARE motorph_expand FROM @motorph_sql;
EXECUTE motorph_expand;
DEALLOCATE PREPARE motorph_expand;
SET @motorph_has_column = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='next_login_at');
SET @motorph_sql = IF(@motorph_has_column=0, 'ALTER TABLE users ADD COLUMN next_login_at DATETIME(6) NULL', 'SELECT 1');
PREPARE motorph_expand FROM @motorph_sql;
EXECUTE motorph_expand;
DEALLOCATE PREPARE motorph_expand;

CREATE TABLE IF NOT EXISTS AuthThrottle (
 SourceKey CHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
 SourceMachine VARCHAR(128) NOT NULL,
 FailedAttempts INT NOT NULL DEFAULT 0,
 RetryAfter DATETIME(6) NULL,
 LockedUntil DATETIME(6) NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS AuditChainHead (
 ChainID TINYINT PRIMARY KEY,
 LastEventID BIGINT NOT NULL,
 LastHash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 CONSTRAINT audit_single_chain CHECK (ChainID=1)
) ENGINE=InnoDB;
INSERT IGNORE INTO AuditChainHead(ChainID,LastEventID,LastHash) VALUES(1,0,REPEAT('0',64));

CREATE TABLE IF NOT EXISTS AuditLog (
 EventID BIGINT PRIMARY KEY,
 OccurredAtUTC DATETIME(6) NOT NULL,
 ActorID VARCHAR(128) NOT NULL,
 Action VARCHAR(64) NOT NULL,
 TargetID VARCHAR(128) NOT NULL,
 Outcome VARCHAR(32) NOT NULL,
 SourceMachine VARCHAR(128) NOT NULL,
 Details VARCHAR(512) NOT NULL,
 PreviousHash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 EntryHash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 INDEX audit_security_events(Action,EventID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS AttendanceRequests (
 RequestID BIGINT AUTO_INCREMENT PRIMARY KEY,
 EmployeeID INT NOT NULL,
 DateFrom DATE NOT NULL,
 DateTo DATE NOT NULL,
 Status VARCHAR(32) NOT NULL,
 RecordData JSON NOT NULL,
 UNIQUE KEY attendance_exact_request(EmployeeID,DateFrom,DateTo),
 CONSTRAINT attendance_request_employee FOREIGN KEY(EmployeeID) REFERENCES employees(EmployeeID),
 CONSTRAINT attendance_request_dates CHECK(DateTo>=DateFrom),
 CONSTRAINT attendance_request_shape CHECK(JSON_TYPE(RecordData)='ARRAY' AND JSON_LENGTH(RecordData)=20)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS PayrollLedger (
 LedgerID BIGINT AUTO_INCREMENT PRIMARY KEY,
 TransactionID VARCHAR(64) NOT NULL UNIQUE,
 EmployeeID INT NOT NULL,
 DateFrom DATE NOT NULL,
 DateTo DATE NOT NULL,
 Status VARCHAR(32) NOT NULL,
 RecordData JSON NOT NULL,
 UNIQUE KEY payroll_exact_period(EmployeeID,DateFrom,DateTo),
 CONSTRAINT payroll_ledger_employee FOREIGN KEY(EmployeeID) REFERENCES employees(EmployeeID),
 CONSTRAINT payroll_ledger_dates CHECK(DateTo>=DateFrom),
 CONSTRAINT payroll_ledger_shape CHECK(JSON_TYPE(RecordData)='ARRAY' AND JSON_LENGTH(RecordData)=22)
) ENGINE=InnoDB;

-- Current administrator becomes the routine DEFINER. Grant app EXECUTE on the
-- routine and SELECT on AuditLog/AuditChainHead; grant NO direct DML on either.
-- The routine NEVER commits. Its append/head update share the business transaction.
DROP PROCEDURE IF EXISTS motorph_append_audit;
DELIMITER $$
CREATE PROCEDURE motorph_append_audit(
 IN p_actor VARCHAR(128), IN p_action VARCHAR(64), IN p_target VARCHAR(128),
 IN p_outcome VARCHAR(32), IN p_source VARCHAR(128), IN p_details VARCHAR(512)
)
SQL SECURITY DEFINER
MODIFIES SQL DATA
BEGIN
 DECLARE v_id BIGINT;
 DECLARE v_previous CHAR(64);
 DECLARE v_hash CHAR(64);
 DECLARE v_time DATETIME(6);
 IF @@autocommit<>0 THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Audit append requires an explicit caller transaction';
 END IF;
 IF p_actor IS NULL OR p_action IS NULL OR p_target IS NULL OR p_outcome IS NULL OR p_source IS NULL OR p_details IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Audit fields cannot be null';
 END IF;
 SELECT LastEventID+1,LastHash INTO v_id,v_previous FROM AuditChainHead WHERE ChainID=1 FOR UPDATE;
 IF v_id IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Audit chain head is missing';
 END IF;
 SET v_time=UTC_TIMESTAMP(6);
 SET v_hash=SHA2(CONCAT(v_previous,'|',v_id,'|',DATE_FORMAT(v_time,'%Y-%m-%dT%H:%i:%s.%fZ'),'|',
  HEX(CONVERT(p_actor USING utf8mb4)),'|',HEX(CONVERT(p_action USING utf8mb4)),'|',HEX(CONVERT(p_target USING utf8mb4)),'|',
  HEX(CONVERT(p_outcome USING utf8mb4)),'|',HEX(CONVERT(p_source USING utf8mb4)),'|',HEX(CONVERT(p_details USING utf8mb4))),256);
 INSERT INTO AuditLog(EventID,OccurredAtUTC,ActorID,Action,TargetID,Outcome,SourceMachine,Details,PreviousHash,EntryHash)
 VALUES(v_id,v_time,p_actor,p_action,p_target,p_outcome,p_source,p_details,v_previous,v_hash);
 UPDATE AuditChainHead SET LastEventID=v_id,LastHash=v_hash WHERE ChainID=1;
END$$
DELIMITER ;

-- App grants required by supported code (administrator supplies actual account):
-- SELECT/INSERT/UPDATE AuthThrottle; SELECT/INSERT/UPDATE/DELETE both ledgers.
-- users SELECT/INSERT; UPDATE(password,must_change_password,failed_attempts,locked_until,next_login_at).
-- Existing employee/business-table grants remain scoped to required CRUD.
-- EXECUTE motorph_append_audit; SELECT AuditLog/AuditChainHead only.
-- DBA/modified-client honesty, external archive anchors, and source-machine trust
-- are separate assurance boundaries; a shared JDBC account cannot attest humans.

-- ADMINISTRATOR ONLY. Run against the isolated, backed-up Milestone 2 clone.
-- This expansion is idempotent. It never converts a SHA-256 digest into BCrypt.
-- Existing non-BCrypt accounts remain unusable until the controlled enrollment
-- tool replaces each legacy credential with a unique temporary BCrypt hash.

SET @motorph_password_width = (
    SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'password'
);
SET @motorph_migration_sql = IF(@motorph_password_width < 255,
    'ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL',
    'SELECT 1');
PREPARE motorph_migration FROM @motorph_migration_sql;
EXECUTE motorph_migration;
DEALLOCATE PREPARE motorph_migration;

SET @motorph_has_reset_column = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'must_change_password'
);
SET @motorph_migration_sql = IF(@motorph_has_reset_column = 0,
    'ALTER TABLE users ADD COLUMN must_change_password TINYINT(1) NOT NULL DEFAULT 1',
    'SELECT 1');
PREPARE motorph_migration FROM @motorph_migration_sql;
EXECUTE motorph_migration;
DEALLOCATE PREPARE motorph_migration;

-- Retry-safe enrollment is performed by the protected administrator tool:
-- 1. Select only accounts whose stored credential is not supported BCrypt12.
-- 2. Generate a fresh per-account temporary password using PasswordService.
-- 3. Hash the plaintext temporary password directly using hashPassword(char[]).
-- 4. Persist hash plus must_change_password=1 in the same transaction.
-- 5. Keep the temporary-password mapping in protected administrator escrow.
-- Already enrolled/reset BCrypt accounts must not be overwritten on retries.
-- Verify aggregate counts and authenticate/reset with dedicated disposable users.
-- Rollback uses the protected preimplementation backup on the isolated clone;
-- do not contract this schema or write old hashes back into the shared database.

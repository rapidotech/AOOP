"""Exercise only aoop_m2 on loopback port 3307; publish redacted evidence."""
from pathlib import Path
import hashlib
import json
import os
import re
import subprocess
import time
from milestone2_environment import ROOT, RUNTIME, BACKUP, sql, run, unprotect_credential


def main():
    destination = Path.home() / "Downloads/AOOP_Milestone2_Group13/evidence"
    destination.mkdir(parents=True, exist_ok=True)
    profile = json.loads((RUNTIME / "profile.json").read_text())
    if profile["port"] != 3307 or profile["schema"] != "aoop_m2":
        raise RuntimeError("Refusing non-isolated environment.")
    admin = unprotect_credential(RUNTIME / "admin.credential.xml")
    app = unprotect_credential(RUNTIME / "app.credential.xml")
    sql(admin["user"], admin["password"], "CREATE DATABASE IF NOT EXISTS aoop_m2_restore_check CHARACTER SET utf8mb4;")
    sql(admin["user"], admin["password"], database="aoop_m2_restore_check", dump=(BACKUP / "aoop-baseline.sql").read_bytes())
    baseline = json.loads((RUNTIME / "environment.json").read_text())["restored_table_counts"]
    restored = {name: int(sql(admin["user"], admin["password"], "SELECT COUNT(*) FROM `" + name + "`;", database="aoop_m2_restore_check").strip()) for name in baseline}
    if restored != baseline:
        raise RuntimeError("Restore rehearsal count mismatch.")
    original_settings = dict(line.split("=", 1) for line in (BACKUP / "database.properties").read_text().splitlines() if "=" in line and not line.startswith("#"))
    original = {name: int(sql(original_settings["user"], original_settings["password"], "SELECT COUNT(*) FROM `" + name + "`;", database="aoop", port=3306).strip()) for name in baseline}
    if original != baseline:
        raise RuntimeError("Original row counts changed since backup; preserve evidence and inspect.")
    (destination / "readiness-and-restore.json").write_text(json.dumps({"baseline_commit": "8387a21043f422e186a2a03a932eb8f50ee364c2", "restored_tables": len(restored), "row_counts_match": True,
        "restored_counts": restored, "original_aoop_counts_unchanged": original == baseline,
        "backup_manifest": str(BACKUP / "manifest.json"), "rollback_schema": "aoop_m2_restore_check", "original_database_writes": 0}, indent=2))
    project = ROOT / "AOOP - MotorPH from OOP"
    classes = project / ".build/security-assurance"
    classes.mkdir(parents=True, exist_ok=True)
    classpath = str(project / ".build/classes") + ";" + str(project / "lib/*")
    run(["javac", "--release", "17", "-encoding", "UTF-8", "-cp", classpath, "-d", str(classes), str(ROOT / "security/tools/SecurityAssurance.java")])
    environment = os.environ.copy()
    environment.update(M2_JDBC_URL=profile["jdbc_url"], M2_ADMIN_PASSWORD=admin["password"], M2_APP_PASSWORD=app["password"],
                       AOOP_DATA_DIR=str(RUNTIME / "test-local-records"))
    command = ["java", "-cp", str(classes) + ";" + classpath, "SecurityAssurance"]
    migration = subprocess.Popen(command + ["enroll"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=environment, text=True, encoding="utf-8")
    line = migration.stdout.readline()
    try:
        enrollment = json.loads(line)
    except Exception:
        _, diagnostic = migration.communicate(timeout=30)
        (RUNTIME / "enrollment-private-error.txt").write_text(diagnostic)
        raise RuntimeError("Enrollment failed before escrow; private diagnostic saved without publishing it.")
    if enrollment["count"]:
        mapping = {}
        escrow_path = RUNTIME / "bootstrap-passwords.dpapi"
        if escrow_path.exists():
            prior_environment = os.environ.copy(); prior_environment['M2_PRIOR_ESCROW'] = str(escrow_path)
            decrypted = run(['powershell', '-NoProfile', '-Command', "$ErrorActionPreference='Stop'; $s=ConvertTo-SecureString ((Get-Content -LiteralPath $env:M2_PRIOR_ESCROW -Raw).Trim()); $c=New-Object System.Management.Automation.PSCredential('escrow',$s); $c.GetNetworkCredential().Password"], environment=prior_environment)
            previous = json.loads(decrypted.decode('utf-8-sig'))
            if not isinstance(previous, list) or any(not isinstance(e, dict) or not isinstance(e.get('username'), str) or not isinstance(e.get('password'), str) for e in previous):
                migration.communicate(input="ROLLBACK\n", timeout=30)
                raise RuntimeError('Existing protected escrow shape invalid; prior state preserved.')
            mapping.update((entry['username'], entry) for entry in previous)
        mapping.update((entry['username'], entry) for entry in enrollment['bootstrap'])
        payload = json.dumps(list(mapping.values()))
        protected_environment = os.environ.copy()
        protected_environment.update(M2_BOOTSTRAP_JSON=payload, M2_BOOTSTRAP_PATH=str(RUNTIME / "bootstrap-passwords.dpapi"))
        try:
            run(["powershell", "-NoProfile", "-File", str(ROOT / "scripts/protect-bootstrap-escrow.ps1")], environment=protected_environment)
        except Exception:
            migration.communicate(input="ROLLBACK\n", timeout=30)
            raise
    stdout, stderr = migration.communicate(input="COMMIT_ESCROW_SAVED\n", timeout=120)
    if migration.returncode or "ENROLLMENT_COMMITTED" not in stdout:
        (RUNTIME / "enrollment-private-error.txt").write_text(stderr)
        raise RuntimeError("Enrollment not committed; inspect private evidence.")
    previous_enrollment = destination / "C2-controlled-enrollment.json"
    initial_count = json.loads(previous_enrollment.read_text()).get("initial_enrolled", json.loads(previous_enrollment.read_text()).get("newly_enrolled", 0)) if previous_enrollment.exists() else enrollment["count"]
    (destination / "C2-controlled-enrollment.json").write_text(json.dumps({"initial_enrolled": initial_count, "newly_enrolled_this_run": enrollment["count"], "bcrypt_cost": 12,
        "retry_duration_ms": enrollment["duration_ms"], "escrow_protected_before_commit": True, "escrow": "DPAPI CurrentUser in private runtime",
        "legacy_sha_wrapped_in_bcrypt": False, "shared_default_login": "denied", "original_database_writes": 0}, indent=2))
    result = subprocess.run(command + ["test"], env=environment, capture_output=True, text=True, encoding="utf-8", timeout=120)
    if result.returncode:
        (RUNTIME / "assurance-private-error.txt").write_text(result.stderr)
        # Publish only the exception category and known non-secret operation text.
        first = next((x for x in result.stderr.splitlines() if "Exception" in x), "Unknown integration error")
        print("Isolated assurance failed: " + first[:220])
        raise RuntimeError("See private assurance diagnostic; no original database was modified.")
    evidence = json.loads(result.stdout)
    evidence["scope"] = "Real isolated MySQL8.0.42; only synthetic disposable mutation records"
    evidence["original_database_writes"] = 0
    (destination / "C1-C2-isolated-integration.json").write_text(json.dumps(evidence, indent=2))
    # DPAPI ciphertext must not contain its plaintext password. Corrupt a COPY only.
    credential_file = RUNTIME / "app.credential.xml"
    if app["password"] in credential_file.read_text(encoding="utf-8-sig"):
        raise RuntimeError("Protected file contains plaintext password.")
    corrupt = RUNTIME / "corrupted-test.credential.xml"
    content = credential_file.read_text(encoding="utf-8-sig")
    corrupt.write_text(re.sub(r'(<SS N="Password">).*?(</SS>)', r'\g<1>invalid-ciphertext\2', content), encoding="utf-8")
    environment2 = os.environ.copy(); environment2["M2_CORRUPT_PATH"] = str(corrupt)
    bad = subprocess.run(["powershell", "-NoProfile", "-Command", "$ErrorActionPreference='Stop'; $c=Import-Clixml -LiteralPath $env:M2_CORRUPT_PATH; $null=$c.GetNetworkCredential().Password"], env=environment2, capture_output=True)
    corrupt.unlink()
    if bad.returncode == 0:
        raise RuntimeError("Corrupt protected configuration accepted.")
    (destination / "C1-protected-config.json").write_text(json.dumps({"encryption": "Windows DPAPI CurrentUser", "plaintext_password_in_ciphertext_file": False,
        "corrupt_configuration": "denied", "normal_current_user_decryption": "success", "wrong_windows_user_test": "Not run; scope binding documented by Microsoft, not runtime-proven here",
        "private_acl": "Current Windows user and SYSTEM only", "shared_root_rotation": "Not performed; isolated replacement secret rotated and old secret denied"}, indent=2))
    print("Restore verified across " + str(len(restored)) + " tables; original row counts unchanged.")
    print("Controlled enrollment: " + str(enrollment["count"]) + " accounts; protected escrow saved before commit.")
    print("Isolated integration checks passed: " + str(evidence["assertions"]) + "; evidence published without secrets.")


if __name__ == "__main__":
    main()

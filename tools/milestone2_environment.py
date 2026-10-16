"""Create/recover a private, loopback-only Milestone 2 MySQL clone.

Never alters the original aoop schema, MySQL service, or original accounts.
Secrets are sent through process input/environment, never command arguments.
"""
from pathlib import Path
import hashlib
import json
import os
import secrets
import socket
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
MYSQL = Path(r"C:\Program Files\MySQL\MySQL Server 8.0\bin")
RUNTIME = Path.home() / ".motorph/milestone2"
BACKUP = Path.home() / "Downloads/AOOP_Milestone2_Group13/private-backup/preimplementation-20261017"
PORT = 3307


def run(command, *, input_bytes=None, environment=None, timeout=90):
    result = subprocess.run(command, input=input_bytes, env=environment,
                            capture_output=True, timeout=timeout)
    if result.returncode:
        raise RuntimeError("Milestone 2 operation failed; raw diagnostics withheld to protect secrets: " + Path(str(command[0])).name)
    return result.stdout


def private_directory(path):
    path.mkdir(parents=True, exist_ok=True)
    sid = run(["powershell", "-NoProfile", "-Command",
               "[System.Security.Principal.WindowsIdentity]::GetCurrent().User.Value"]).decode().strip()
    run(["icacls", str(path), "/inheritance:r", "/grant:r", "*" + sid + ":(OI)(CI)F", "*S-1-5-18:(OI)(CI)F"])


def protect_credential(path, user, password):
    environment = os.environ.copy()
    environment.update(M2_PRIVATE_USER=user, M2_PRIVATE_PASSWORD=password, M2_PRIVATE_PATH=str(path))
    run(["powershell", "-NoProfile", "-Command",
         "$ErrorActionPreference='Stop'; $s=ConvertTo-SecureString $env:M2_PRIVATE_PASSWORD -AsPlainText -Force; "
         "$c=New-Object System.Management.Automation.PSCredential($env:M2_PRIVATE_USER,$s); "
         "$c | Export-Clixml -LiteralPath $env:M2_PRIVATE_PATH -Encoding UTF8"], environment=environment)


def unprotect_credential(path):
    environment = os.environ.copy()
    environment["M2_PRIVATE_PATH"] = str(path)
    content = run(["powershell", "-NoProfile", "-Command",
                   "$ErrorActionPreference='Stop'; $c=Import-Clixml -LiteralPath $env:M2_PRIVATE_PATH; "
                   "@{user=$c.UserName;password=$c.GetNetworkCredential().Password}|ConvertTo-Json -Compress"], environment=environment)
    return json.loads(content)


def sql(user, password, statement=None, database=None, dump=None, port=PORT):
    client = RUNTIME / ("client-" + secrets.token_hex(8) + ".cnf")
    # Generated secrets contain no quote/backslash characters. Original config is escaped.
    escaped = password.replace("\\", "\\\\").replace('"', '\\"')
    client.write_text('[client]\nuser="' + user + '"\npassword="' + escaped
                      + '"\nhost=127.0.0.1\nport=' + str(port) + '\nssl-mode=REQUIRED\n', encoding="utf-8")
    try:
        command = [str(MYSQL / "mysql.exe"), "--defaults-file=" + str(client), "--batch", "--raw", "--skip-column-names"]
        if database:
            command.append("--database=" + database)
        payload = dump if dump is not None else statement.encode("utf-8")
        return run(command, input_bytes=payload).decode("utf-8")
    finally:
        client.unlink(missing_ok=True)


def main():
    private_directory(RUNTIME)
    marker = RUNTIME / "environment.json"
    if marker.exists():
        state = json.loads(marker.read_text())
        if state.get("port") != PORT or state.get("schema") != "aoop_m2":
            raise RuntimeError("Existing environment identity mismatch; refusing changes.")
        print("Existing isolated environment preserved: " + str(RUNTIME))
        return
    with socket.socket() as probe:
        if probe.connect_ex(("127.0.0.1", PORT)) == 0:
            raise RuntimeError("Isolated port already occupied; original instances preserved.")
    dump = BACKUP / "aoop-baseline.sql"
    if not dump.is_file() or dump.stat().st_size == 0:
        raise RuntimeError("Complete baseline backup required before provisioning.")
    data = RUNTIME / "data"
    if data.exists():
        raise RuntimeError("Existing unmarked data directory preserved; inspect before retry.")
    data.mkdir()
    config = RUNTIME / "my.ini"
    config.write_text('[mysqld]\nbasedir="' + str(MYSQL.parent).replace("\\", "/")
                      + '"\ndatadir="' + str(data).replace("\\", "/")
                      + '"\nport=3307\nbind-address=127.0.0.1\nmysqlx=0\nrequire_secure_transport=ON'
                      + '\ninnodb_buffer_pool_size=64M\nlog-error="'
                      + str(RUNTIME / "mysql-error.log").replace("\\", "/")
                      + '"\npid-file="' + str(RUNTIME / "mysql.pid").replace("\\", "/") + '"\n', encoding="utf-8")
    run([str(MYSQL / "mysqld.exe"), "--defaults-file=" + str(config), "--initialize-insecure"], timeout=90)
    root_password = secrets.token_urlsafe(36)
    protect_credential(RUNTIME / "admin.credential.xml", "root", root_password)
    initialization = RUNTIME / "initial-root.sql"
    initialization.write_text("ALTER USER 'root'@'localhost' IDENTIFIED BY '" + root_password + "';\n", encoding="utf-8")
    # Use PowerShell's hidden process launcher: no service registration or shared-server restart.
    environment = os.environ.copy()
    environment.update(M2_MYSQL=str(MYSQL / "mysqld.exe"), M2_MYINI=str(config), M2_INIT=str(initialization))
    run(["powershell", "-NoProfile", "-Command",
         "$argsList=@(('\"--defaults-file='+$env:M2_MYINI+'\"'),('\"--init-file='+$env:M2_INIT+'\"')); "
         "Start-Process -FilePath $env:M2_MYSQL -ArgumentList $argsList -WindowStyle Hidden"], environment=environment)
    ready = False
    for _ in range(80):
        try:
            if sql("root", root_password, "SELECT @@port;").strip() == str(PORT):
                ready = True
                break
        except Exception:
            time.sleep(0.25)
    if not ready:
        raise RuntimeError("Isolated instance not ready; inspect its private error log.")
    initialization.unlink()
    sql("root", root_password, "CREATE DATABASE aoop_m2 CHARACTER SET utf8mb4;")
    sql("root", root_password, database="aoop_m2", dump=dump.read_bytes())
    tables = sql("root", root_password, "SHOW TABLES;", database="aoop_m2").splitlines()
    counts = {table: int(sql("root", root_password, "SELECT COUNT(*) FROM `" + table + "`;", database="aoop_m2").strip()) for table in tables}
    state = {"schema": "aoop_m2", "host": "127.0.0.1", "port": PORT,
             "runtime": str(RUNTIME), "mysql_version": "8.0.42", "baseline_commit": "8387a21043f422e186a2a03a932eb8f50ee364c2",
             "dump_sha256": hashlib.sha256(dump.read_bytes()).hexdigest(), "restored_table_counts": counts,
             "original_database_unchanged": True}
    marker.write_text(json.dumps(state, indent=2))
    print("Isolated TLS-required instance restored on 127.0.0.1:3307, schema aoop_m2.")
    print("Restored tables: " + str(len(tables)) + "; counts saved in private environment.json.")


if __name__ == "__main__":
    main()

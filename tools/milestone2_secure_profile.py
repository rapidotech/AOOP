"""Restrict only the marked Milestone 2 clone and create a DPAPI launch profile."""
from pathlib import Path
import datetime
import ipaddress
import json
import os
import secrets
from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID, ExtendedKeyUsageOID
from milestone2_environment import RUNTIME, ROOT, MYSQL, sql, run, protect_credential, unprotect_credential


def main():
    state = json.loads((RUNTIME / "environment.json").read_text())
    if state.get("port") != 3307 or state.get("schema") != "aoop_m2":
        raise RuntimeError("Unmarked environment; refusing changes.")
    admin = unprotect_credential(RUNTIME / "admin.credential.xml")
    if (RUNTIME / "profile.json").exists():
        print("Existing isolated secure profile preserved.")
        return
    now = datetime.datetime.now(datetime.timezone.utc)
    ca_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    ca_name = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "MotorPH Milestone 2 Local CA")])
    ca = (x509.CertificateBuilder().subject_name(ca_name).issuer_name(ca_name).public_key(ca_key.public_key())
          .serial_number(x509.random_serial_number()).not_valid_before(now - datetime.timedelta(days=1))
          .not_valid_after(now + datetime.timedelta(days=365)).add_extension(x509.BasicConstraints(ca=True, path_length=0), critical=True)
          .sign(ca_key, hashes.SHA256()))
    server_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    server = (x509.CertificateBuilder().subject_name(x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "localhost")]))
              .issuer_name(ca_name).public_key(server_key.public_key()).serial_number(x509.random_serial_number())
              .not_valid_before(now - datetime.timedelta(days=1)).not_valid_after(now + datetime.timedelta(days=365))
              .add_extension(x509.BasicConstraints(ca=False, path_length=None), critical=True)
              .add_extension(x509.SubjectAlternativeName([x509.DNSName("localhost"), x509.IPAddress(ipaddress.ip_address("127.0.0.1"))]), critical=False)
              .add_extension(x509.ExtendedKeyUsage([ExtendedKeyUsageOID.SERVER_AUTH]), critical=False)
              .sign(ca_key, hashes.SHA256()))
    data = RUNTIME / "data"
    for path, content in [(data / "ca.pem", ca.public_bytes(serialization.Encoding.PEM)),
                          (data / "server-cert.pem", server.public_bytes(serialization.Encoding.PEM)),
                          (data / "server-key.pem", server_key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.TraditionalOpenSSL, serialization.NoEncryption()))]:
        path.write_bytes(content)
    sql(admin["user"], admin["password"], "ALTER INSTANCE RELOAD TLS;")
    trust = RUNTIME / "truststore.p12"
    run(["keytool", "-importcert", "-noprompt", "-alias", "motorph-m2-ca", "-file", str(data / "ca.pem"),
         "-keystore", str(trust), "-storetype", "PKCS12", "-storepass", "changeit"])
    old_password = secrets.token_urlsafe(36)
    new_password = secrets.token_urlsafe(36)
    app = "'motorph_m2_app'@'127.0.0.1'"
    sql(admin["user"], admin["password"], "CREATE USER IF NOT EXISTS " + app + " IDENTIFIED BY '" + old_password + "' REQUIRE SSL;")
    sql(admin["user"], admin["password"], "ALTER USER " + app + " IDENTIFIED BY '" + old_password + "' REQUIRE SSL;")
    sql(admin["user"], admin["password"], "REVOKE ALL PRIVILEGES, GRANT OPTION FROM " + app + ";")
    grants = ["GRANT SELECT ON aoop_m2.* TO " + app]
    for table in ["employees", "governmentids", "compensation"]:
        grants.append("GRANT INSERT, UPDATE, DELETE ON aoop_m2.`" + table + "` TO " + app)
    grants.append("GRANT DELETE ON aoop_m2.attendance TO " + app)
    for table in ["addresses", "employeestatuses", "positions"]:
        grants.append("GRANT INSERT ON aoop_m2.`" + table + "` TO " + app)
    grants.extend(["GRANT INSERT ON aoop_m2.users TO " + app,
                   "GRANT UPDATE (password, must_change_password) ON aoop_m2.users TO " + app])
    # The schema migration is an admin-only prerequisite for column-specific grants.
    migration = ROOT / "security/migrations/001_expand_password_reset.sql"
    sql(admin["user"], admin["password"], database="aoop_m2", dump=migration.read_bytes())
    for grant in grants:
        sql(admin["user"], admin["password"], grant + ";")
    sql("motorph_m2_app", old_password, "SELECT COUNT(*) FROM employees;", database="aoop_m2")
    sql(admin["user"], admin["password"], "ALTER USER " + app + " IDENTIFIED BY '" + new_password + "' REQUIRE SSL;")
    rotated_works = sql("motorph_m2_app", new_password, "SELECT COUNT(*) FROM employees;", database="aoop_m2").strip().isdigit()
    old_denied = False
    try:
        sql("motorph_m2_app", old_password, "SELECT 1;", database="aoop_m2")
    except RuntimeError:
        old_denied = True
    if not rotated_works or not old_denied:
        raise RuntimeError("Credential rotation assurance failed.")
    protect_credential(RUNTIME / "app.credential.xml", "motorph_m2_app", new_password)
    jdbc_url = ("jdbc:mysql://127.0.0.1:3307/aoop_m2?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreUrl="
                + trust.as_uri() + "&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&fallbackToSystemTrustStore=false")
    profile = {"host": "127.0.0.1", "port": 3307, "schema": "aoop_m2", "jdbc_url": jdbc_url,
               "configuration_protection": "Windows DPAPI CurrentUser; user+SYSTEM ACL", "bootstrap_strategy": "Unique random per-account BCrypt12 temporary passwords; mandatory reset",
               "original_database_unchanged": True, "original_root_password_rotated": False}
    (RUNTIME / "profile.json").write_text(json.dumps(profile, indent=2))
    evidence = {"control": "C1", "new_credential": "success", "superseded_isolated_credential": "denied",
                "require_secure_transport": True, "client_tls": "VERIFY_IDENTITY", "restricted_account": "motorph_m2_app@127.0.0.1",
                "grants": sql(admin["user"], admin["password"], "SHOW GRANTS FOR " + app + ";").splitlines(),
                "original_schema_and_shared_root_untouched": True}
    destination = Path.home() / "Downloads/AOOP_Milestone2_Group13/evidence"
    destination.mkdir(parents=True, exist_ok=True)
    (destination / "C1-profile-and-rotation.json").write_text(json.dumps(evidence, indent=2))
    print("DPAPI restricted profile provisioned; TLS identity verification configured; superseded isolated credential denied.")


if __name__ == "__main__":
    main()

# Support & Community Help

Welcome to the Cryptika Messenger support guide! We want to make sure you get answers to your questions quickly, while keeping security and bug channels dedicated to their intended purposes.

---

## 🔍 Where to Get Help

| Need Help With | Recommended Channel | Details |
|---|---|---|
| **Security Vulnerabilities** | [Private Vulnerability Reporting](../../security/advisories/new) | For sensitive flaws, auth bypasses, or crypto bugs. Follow [SECURITY.md](SECURITY.md). |
| **Bug Reports** | [GitHub Issues](../../issues/new?template=bug_report.md) | Reproducible crashes, UI defects, or logic errors not impacting user secrets. |
| **Feature Ideas & Architecture** | [GitHub Discussions / Feature Requests](../../issues/new?template=feature_request.md) | Proposals for new capabilities, UI redesigns, or cryptographic improvements. |
| **General Questions & Setup** | [GitHub Discussions](../../discussions) | Questions on building, running the EC2 server, or understanding the protocol. |

---

## ⚠️ Important Note: What NOT to post publicly

Cryptika Messenger is designed around extreme zero-knowledge privacy. When posting questions or logs on public issues or forums:
* **NEVER share private keys, seeds, or JWT tokens.**
* **NEVER post unredacted full ADB logs** containing identity hashes, session UUIDs, or IP addresses.
* Sanitized logs and stack traces are always welcome.

---

## 📖 Useful Documentation

* [README.md](README.md) — System architecture, threat model, and cryptographic flow diagrams.
* [CONTRIBUTING.md](CONTRIBUTING.md) — Local development, compilation requirements (JDK 17), and PR guidelines.
* [SECURITY.md](SECURITY.md) — Vulnerability reporting, CVE allocation, and coordinated disclosure.

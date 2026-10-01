# Security Policy

The Cryptika team takes the security and privacy of our users and their cryptographic data with utmost seriousness. We appreciate the efforts of security researchers and community members who practice responsible and coordinated vulnerability disclosure.

---

## Supported Versions

Only the latest active major or minor release line receives security updates and patches.

| Version | Supported | Status |
| ------- | --------- | ------ |
| 3.0.x   | Yes       | Currently Supported and Active |
| < 3.0.0 | No        | End of Life and Unsupported |

---

## Reporting a Vulnerability

Please do not file public GitHub issues or pull requests for security vulnerabilities.

### Method 1: GitHub Private Vulnerability Reporting (Preferred)
GitHub provides a secure, private disclosure channel directly within this repository:
1. Navigate to the Security tab of the repository.
2. Under Vulnerability reporting, click Report a vulnerability.
3. Provide a clear description, affected components, proof of concept steps, and expected impact.
4. Submit the report. A private discussion advisory will be opened between you and the project maintainers.

### Method 2: Security Contact Email
If you are unable to use the private reporting tool on GitHub, you may email the maintainers directly at:
* Contact: arungaming1973@gmail.com
* Subject: [SECURITY] Cryptika Vulnerability Report - Brief Description

---

## In Scope Components

* Android Client (/app):
  * Cryptographic handshakes (X25519 Ephemeral DH, Ed25519 signing and verification)
  * Message encryption and ratchet mechanics (ChaCha20-Poly1305, HashRatchet)
  * Local key security (Android Keystore hardware-backed wrapping, SQLCipher database zeroization)
  * Memory hygiene, clipboard security, and screenshot protections (FLAG_SECURE, Secure Input Field)
  * Real-time VoIP calling state machine and directional audio encryption
* Blind Relay Server (/server):
  * Passwordless authentication and ephemeral JWT lifecycle
  * Room routing, participant isolation, and memory exhaustion or denial of service
  * Ephemeral session lifetime and bidirectional PEER_DISCONNECTED signaling

---

## Response Timelines and SLAs

* Initial Response: Within 24 to 48 hours acknowledging receipt of the report.
* Triage and Assessment: Within 3 to 5 business days confirming reproducibility, severity, and impacted versions.
* Remediation and Patching: High-severity vulnerabilities are prioritized for hotfix releases, typically within 7 to 14 days.
* Coordinated Disclosure: We adhere to standard coordinated vulnerability disclosure (90 days, or upon public release of the patch).

---

## CVE Assignment and Researcher Attribution

Cryptika is an open source project hosted on GitHub:
* When a confirmed, eligible vulnerability is reported and patched, project maintainers will create a GitHub Security Advisory.
* Maintainers will request an official CVE ID directly through GitHub's CVE Numbering Authority.
* Credit and Attribution: Reporters will be formally credited in:
  1. The official GitHub Security Advisory.
  2. The CVE record description.
  3. The project release notes and CHANGELOG.
  4. The project Hall of Fame when requested.

Thank you for helping keep Cryptika and its users safe.

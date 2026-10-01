# Security Policy

The Cryptika team takes the security and privacy of our users and their cryptographic data with utmost seriousness. We appreciate the efforts of security researchers and community members who practice responsible and coordinated vulnerability disclosure.

---

## 🛡️ Supported Versions

Only the latest active major/minor release line receives security updates and patches.

| Version | Supported          | Status                                 |
| ------- | ------------------ | -------------------------------------- |
| 3.0.x   | :white_check_mark: | Currently Supported (Active)           |
| < 3.0.0 | :x:                | End of Life (Unsupported)              |

---

## 🔒 Reporting a Vulnerability

**Please do NOT file public GitHub Issues or PRs for security vulnerabilities.**

### Method 1: GitHub Private Vulnerability Reporting (Preferred)
GitHub provides a secure, private disclosure channel directly within this repository:
1. Navigate to the **[Security](../../security)** tab of the repository.
2. Under **Vulnerability reporting**, click **"Report a vulnerability"**.
3. Provide a clear description, affected components, proof-of-concept (PoC) steps, and expected impact.
4. Submit the report. A private discussion advisory will be opened between you and the project maintainers.

### Method 2: Security Contact Email
If you are unable to use GitHub's private reporting tool, you may email the maintainers directly at:
* **Contact:** `arungaming1973@gmail.com`
* **Subject:** `[SECURITY] Cryptika Vulnerability Report - <Brief Description>`

---

## 🎯 In-Scope Components

* **Android Client (`/app`):**
  * Cryptographic handshakes (X25519 Ephemeral DH, Ed25519 signing/verification)
  * Message encryption and ratchet mechanics (ChaCha20-Poly1305, HashRatchet)
  * Local key security (Android Keystore hardware-backed wrapping, SQLCipher database zeroization)
  * Memory hygiene, clipboard security, and screenshot protections (`FLAG_SECURE`, Secure Input Field)
  * Real-time VoIP calling state machine & directional audio encryption
* **Blind Relay Server (`/server`):**
  * Passwordless authentication & ephemeral JWT lifecycle
  * Room routing, participant isolation, and memory exhaustion / DoS
  * Ephemeral session lifetime & bidirectional `0xFF 0xFE PEER_DISCONNECTED` signaling

---

## ⏱️ Response Timelines & SLAs

* **Initial Response:** Within **24–48 hours** acknowledging receipt of the report.
* **Triage & Assessment:** Within **3–5 business days** confirming reproducibility, severity, and impacted versions.
* **Remediation & Patching:** High-severity vulnerabilities are prioritized for hotfix releases, typically within **7–14 days**.
* **Coordinated Disclosure:** We adhere to standard coordinated vulnerability disclosure (90 days, or upon public release of the patch).

---

## 🏷️ CVE Assignment & Researcher Attribution

Cryptika is an open-source project hosted on GitHub:
* When a confirmed, eligible vulnerability is reported and patched, project maintainers will create a **GitHub Security Advisory (GHSA)**.
* Maintainers will request an official **CVE ID** directly through **GitHub's CVE Numbering Authority (CNA)**.
* **Credit & Attribution:** Reporters will be formally credited in:
  1. The official GitHub Security Advisory.
  2. The CVE record description.
  3. The project release notes / CHANGELOG.
  4. The project Hall of Fame (if requested).

Thank you for helping keep Cryptika and its users safe!

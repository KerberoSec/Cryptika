# Contributing to Cryptika Messenger

Thank you for your interest in contributing to Cryptika Messenger. As an open source, privacy-focused, zero-knowledge messaging and calling system, we welcome contributions from developers, cryptographers, security researchers, and designers.

Please review this document before submitting issues, feature suggestions, or pull requests.

---

## Code of Conduct

All contributors and community members are expected to follow our [Code of Conduct](CODE_OF_CONDUCT.md). Please report any unacceptable behavior to `arungaming1973@gmail.com`.

---

## Security Vulnerabilities

Please do not report security vulnerabilities through public GitHub issues or pull requests.

If you have discovered a vulnerability, please read our [Security Policy](SECURITY.md) and report it via:
* GitHub Private Vulnerability Reporting: [Submit a private report](../../security/advisories/new)
* Direct Security Email: `arungaming1973@gmail.com`

Confirmed vulnerabilities are eligible for formal CVE allocation through GitHub's CVE Numbering Authority, and researchers will receive full attribution.

---

## Development Setup and Architecture

Cryptika Messenger consists of two primary components:

1. Android Client (/app):
   * Language and SDK: Kotlin 1.9.22, Android compileSdk 34, minSdk 26
   * UI Framework: Jetpack Compose with Material 3
   * Cryptography: Ed25519 (Identity and Signatures), X25519 (DH Session Keys), ChaCha20-Poly1305 (Wire and Message AEAD), HashRatchet (Forward Secrecy)
   * Local Storage: Room Database with SQLCipher, Android Keystore hardware-backed wrapping
   * Build Requirements: JDK 17 (JAVA_HOME pointing to JDK 17 is required)

2. Blind Relay Server (/server):
   * Runtime: Node.js 18+ (tested on Node 22)
   * Network: Express REST API and WebSocket binary packet relay
   * Design: Strictly zero knowledge. The server cannot decrypt messages, does not persist chats, and maintains no long-term user database.

### Building the Android App Locally
```bash
# Clone the repository
git clone https://github.com/KerberoSec/Cryptika.git
cd Cryptika

# Ensure JDK 17 is active
export JAVA_HOME=/path/to/jdk-17

# Run unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug
```
The compiled APK will be located at `app/build/outputs/apk/debug/Cryptika-debug.apk`.

### Running the Relay Server Locally
```bash
cd server
npm install
npm start
```

---

## Pull Request Guidelines

To ensure smooth review and integration, please follow these guidelines:

1. Create a Topic Branch: Never work directly on main. Branch from main using descriptive names:
   * fix/session-disconnect-race
   * feat/audio-volume-booster
   * docs/update-threat-model

2. Commit Hygiene:
   * Keep commits atomic and logically separated.
   * Write concise, imperative commit messages.

3. Cryptographic and Documentation Integrity:
   * Never weaken or bypass existing cryptographic checks (such as signature verification, timing attack protections, zeroization of sensitive arrays).
   * Preserve all explanatory inline comments and threat model notes.

4. Verify Locally Before Submitting:
   * Ensure the project compiles without warnings or errors: `./gradlew assembleDebug`
   * Ensure all unit tests pass: `./gradlew test`

5. Submit Pull Request:
   * Complete the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md).
   * Link relevant issues using GitHub keywords (for example: Fixes #12).

---

## Reporting Non Security Bugs

For general bugs (UI defects, crashes not involving secret leakage, performance issues):
* Check existing issues to avoid duplicates.
* Use the [Bug Report Template](.github/ISSUE_TEMPLATE/bug_report.md) and include:
  * Android OS version and device model
  * Steps to reproduce
  * Expected versus actual behavior
  * Relevant Logcat output with personal identifiers removed

---

## Proposing Features

We welcome ideas that enhance privacy and user experience:
* Use the [Feature Request Template](.github/ISSUE_TEMPLATE/feature_request.md).
* Explain the use case, why this benefits Cryptika users, and any potential security or privacy trade-offs.

---

## License

By contributing code to Cryptika, you agree that your contributions will be licensed under the project open source license.

🔐 File Integrity Checker

"Trust, but verify."

A Java command-line tool that generates and verifies SHA-256 checksums for files — detecting unauthorized modifications, corruption, or tampering.

✨ Features
VersionFeatureV1Ask for a file path · Check if file existsV2Generate SHA-256 hash · Save to hashes.txtV3Verification mode · Tamper detection warningV4 ⭐Full menu interface · Timestamps · List all tracked files

🚀 Getting Started
Prerequisites

Java JDK 8 or higher

Compile & Run
bash# Compile
javac src/FileIntegrityChecker.java -d out/

# Run
java -cp out FileIntegrityChecker
Sample Session
  ╔══════════════════════════════════╗
  ║   FILE INTEGRITY CHECKER v4.0   ║
  ║   SHA-256 · Detect · Verify     ║
  ╚══════════════════════════════════╝

  ┌─────────────────────────────┐
  │  1. Generate Hash           │
  │  2. Verify File             │
  │  3. List Tracked Files      │
  │  4. Exit                    │
  └─────────────────────────────┘
  Your choice: 1

  [ GENERATE HASH ]
  ─────────────────────────────────
  Enter file path: test.txt

  File : test.txt
  Size : 1.2 KB
  SHA-256:
    a3f5c6d8e9f12b34c56d78e9f01a23b4c56d78e9f01a23b4c56d78e9f01a23b

  ✔ Hash saved to hashes.txt

🧠 Concepts Used
ConceptWhereFileInputStreamReading files in 8 KB chunksMessageDigestSHA-256 hashing algorithmBufferedWriter / ReaderPersisting and loading hashestry-catchRobust exception handlingwhile loopChunk-by-chunk file processingSimpleDateFormatTimestamping saved hashes

📁 Project Structure
FileIntegrityChecker/
│
├── src/
│   ├── FileIntegrityCheckerV1.java   ← Task 1: file existence check
│   ├── FileIntegrityCheckerV2.java   ← V2: hash generation + save
│   ├── FileIntegrityCheckerV3.java   ← V3: generate + verify modes
│   └── FileIntegrityChecker.java     ← V4: full menu (main file)
│
├── web/
│   └── index.html                    ← Interactive web demo
│
├── hashes.txt                        ← Auto-generated at runtime
├── .gitignore
└── README.md

🔒 How SHA-256 Works (in brief)
SHA-256 is a one-way cryptographic hash function that turns any file into a unique 64-character hex string. Even a single changed byte produces a completely different hash — making it ideal for detecting file tampering.
Original file  →  a3f5c6d8...ef12  ✔ Match
Modified file  →  9b2e1d7a...ca89  ✘ TAMPERED!

🛣️ Roadmap

 V1 — File existence check
 V2 — Hash generation & storage
 V3 — Verification mode
 V4 — Full menu interface with timestamps
 V5 — Swing GUI (browse, hash, verify buttons)


📄 License
MIT — feel free to use, modify, and share.

Built as a Java learning project to explore file I/O, cryptographic hashing, and CLI design.

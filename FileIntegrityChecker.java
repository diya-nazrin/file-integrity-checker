import java.util.Scanner;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
 
/**
 * ╔══════════════════════════════════╗
 * ║   FILE INTEGRITY CHECKER v4.0   ║
 * ║   "Trust, but verify."          ║
 * ╚══════════════════════════════════╝
 *
 * A command-line tool to generate and verify SHA-256
 * checksums for files — detecting unauthorised modifications.
 *
 * Features:
 *   1. Generate SHA-256 hash and persist it
 *   2. Verify a file against its stored hash
 *   3. List all tracked files
 *   4. Exit
 *
 * Author  : Diya
 * Version : 4.0
 */
public class FileIntegrityChecker {
 
    // ── Constants ────────────────────────────────────────────────────────────
 
    private static final String HASH_STORE   = "hashes.txt";
    private static final String DATE_FORMAT  = "yyyy-MM-dd HH:mm:ss";
    private static final int    BUFFER_SIZE  = 8192; // 8 KB
 
    // ── Entry Point ──────────────────────────────────────────────────────────
 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printBanner();
 
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Your choice: ");
            String choice = scanner.nextLine().trim();
 
            switch (choice) {
                case "1":
                    generateMode(scanner);
                    break;
                case "2":
                    verifyMode(scanner);
                    break;
                case "3":
                    listTrackedFiles();
                    break;
                case "4":
                    System.out.println("\n  Goodbye. Stay secure. 🔐\n");
                    running = false;
                    break;
                default:
                    System.out.println("\n  ✘ Invalid option. Please enter 1–4.\n");
            }
        }
 
        scanner.close();
    }
 
    // ── UI Helpers ───────────────────────────────────────────────────────────
 
    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════╗");
        System.out.println("  ║   FILE INTEGRITY CHECKER v4.0   ║");
        System.out.println("  ║   SHA-256 · Detect · Verify     ║");
        System.out.println("  ╚══════════════════════════════════╝");
        System.out.println();
    }
 
    private static void printMenu() {
        System.out.println("  ┌─────────────────────────────┐");
        System.out.println("  │  1. Generate Hash           │");
        System.out.println("  │  2. Verify File             │");
        System.out.println("  │  3. List Tracked Files      │");
        System.out.println("  │  4. Exit                    │");
        System.out.println("  └─────────────────────────────┘");
    }
 
    private static void printDivider() {
        System.out.println("  ─────────────────────────────────");
    }
 
    // ── Mode 1: Generate ─────────────────────────────────────────────────────
 
    private static void generateMode(Scanner scanner) {
        System.out.println("\n  [ GENERATE HASH ]");
        printDivider();
        System.out.print("  Enter file path: ");
        String path = scanner.nextLine().trim();
 
        File file = new File(path);
        if (!fileCheck(file)) return;
 
        try {
            System.out.println("  Computing SHA-256...");
            String hash = generateSHA256(file);
 
            System.out.println();
            System.out.println("  File : " + file.getName());
            System.out.println("  Size : " + formatSize(file.length()));
            System.out.println("  SHA-256:");
            System.out.println("    " + hash);
 
            saveHash(file.getName(), hash);
            System.out.println();
            System.out.println("  ✔ Hash saved to " + HASH_STORE);
            System.out.println();
 
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }
 
    // ── Mode 2: Verify ───────────────────────────────────────────────────────
 
    private static void verifyMode(Scanner scanner) {
        System.out.println("\n  [ VERIFY FILE ]");
        printDivider();
        System.out.print("  Enter file path: ");
        String path = scanner.nextLine().trim();
 
        File file = new File(path);
        if (!fileCheck(file)) return;
 
        try {
            System.out.println("  Computing current SHA-256...");
            String currentHash = generateSHA256(file);
            String savedHash   = loadHash(file.getName());
 
            if (savedHash == null) {
                System.out.println();
                System.out.println("  ✘ No saved hash found for: " + file.getName());
                System.out.println("    Generate a hash first (option 1).");
                System.out.println();
                return;
            }
 
            System.out.println();
            System.out.println("  File         : " + file.getName());
            System.out.println("  Current Hash : " + currentHash);
            System.out.println("  Saved Hash   : " + savedHash);
            System.out.println();
 
            if (currentHash.equals(savedHash)) {
                System.out.println("  ✔ INTEGRITY VERIFIED — file is unchanged.");
            } else {
                System.out.println("  ╔══════════════════════════════════╗");
                System.out.println("  ║   ⚠  WARNING: FILE MODIFIED!  ⚠  ║");
                System.out.println("  ╚══════════════════════════════════╝");
                System.out.println("  The file contents no longer match the stored hash.");
                System.out.println("  It may have been tampered with or corrupted.");
            }
            System.out.println();
 
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }
 
    // ── Mode 3: List Tracked Files ───────────────────────────────────────────
 
    private static void listTrackedFiles() {
        System.out.println("\n  [ TRACKED FILES ]");
        printDivider();
 
        File store = new File(HASH_STORE);
        if (!store.exists()) {
            System.out.println("  No files tracked yet. Generate a hash first.");
            System.out.println();
            return;
        }
 
        try (BufferedReader reader = new BufferedReader(new FileReader(store))) {
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String name = line;
                String hash = reader.readLine();
                String date = reader.readLine(); // date or "---"
                String sep  = (date != null && date.equals("---")) ? date : reader.readLine();
 
                if (name != null && hash != null) {
                    count++;
                    System.out.println("  " + count + ". " + name);
                    System.out.println("     " + abbreviate(hash, 40) + "...");
                    if (date != null && !date.equals("---")) {
                        System.out.println("     Saved: " + date);
                    }
                    System.out.println();
                }
            }
            if (count == 0) System.out.println("  No entries found.\n");
 
        } catch (IOException e) {
            System.out.println("  ✘ Could not read " + HASH_STORE + ": " + e.getMessage());
        }
    }
 
    // ── Core Algorithm ───────────────────────────────────────────────────────
 
    /**
     * Generates a SHA-256 checksum by reading the file in 8 KB chunks.
     * Using chunks (instead of loading the whole file) keeps memory usage
     * constant regardless of file size.
     *
     * @param file  The file to hash
     * @return      Lowercase hex string of the 256-bit digest
     */
    public static String generateSHA256(File file)
            throws IOException, NoSuchAlgorithmException {
 
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
 
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
 
        byte[] hashBytes = digest.digest();
        StringBuilder hex = new StringBuilder();
        for (byte b : hashBytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
 
    // ── Persistence ──────────────────────────────────────────────────────────
 
    /**
     * Saves (or updates) a filename → hash entry in hashes.txt.
     * Format per entry:
     *   <filename>
     *   <sha256hash>
     *   <timestamp>
     *   ---
     */
    private static void saveHash(String fileName, String hash) throws IOException {
        File store = new File(HASH_STORE);
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;
 
        // Read existing entries, replacing the one for this file if found
        if (store.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(store))) {
                String name;
                while ((name = reader.readLine()) != null) {
                    String storedHash = reader.readLine();
                    String ts         = reader.readLine();
                    reader.readLine(); // "---"
 
                    if (name.equals(fileName)) {
                        appendEntry(sb, fileName, hash);
                        replaced = true;
                    } else if (storedHash != null) {
                        sb.append(name).append("\n")
                          .append(storedHash).append("\n")
                          .append(ts).append("\n")
                          .append("---").append("\n");
                    }
                }
            }
        }
 
        if (!replaced) {
            appendEntry(sb, fileName, hash);
        }
 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(store, false))) {
            writer.write(sb.toString());
        }
    }
 
    private static void appendEntry(StringBuilder sb, String fileName, String hash) {
        String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        sb.append(fileName).append("\n")
          .append(hash).append("\n")
          .append(timestamp).append("\n")
          .append("---").append("\n");
    }
 
    /** Returns the stored hash for a filename, or null if not found. */
    private static String loadHash(String fileName) throws IOException {
        File store = new File(HASH_STORE);
        if (!store.exists()) return null;
 
        try (BufferedReader reader = new BufferedReader(new FileReader(store))) {
            String name;
            while ((name = reader.readLine()) != null) {
                String storedHash = reader.readLine();
                reader.readLine(); // timestamp
                reader.readLine(); // "---"
                if (name.equals(fileName)) return storedHash;
            }
        }
        return null;
    }
 
    // ── Small Helpers ─────────────────────────────────────────────────────────
 
    private static boolean fileCheck(File file) {
        if (!file.exists()) {
            System.out.println("\n  ✘ File not found: " + file.getPath());
            System.out.println();
            return false;
        }
        return true;
    }
 
    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / 1048576.0);
    }
 
    private static String abbreviate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}

import java.util.Scanner;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
/**
 * FileIntegrityChecker - Version 3
 * Features:
 *   - Generate SHA-256 hash and save to hashes.txt
 *   - Verify a file against its stored hash
 *   - Warn if the file has been modified
 *
 * Author: Diya
 * Version: 3.0
 */
public class FileIntegrityCheckerV3 {
 
    private static final String HASH_STORE = "hashes.txt";
 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
 
        System.out.println("=============================");
        System.out.println("  FILE INTEGRITY CHECKER V3  ");
        System.out.println("=============================");
        System.out.println();
        System.out.println("1. Generate Hash");
        System.out.println("2. Verify File");
        System.out.println();
        System.out.print("Choose an option (1 or 2): ");
        String choice = scanner.nextLine().trim();
 
        switch (choice) {
            case "1":
                generateMode(scanner);
                break;
            case "2":
                verifyMode(scanner);
                break;
            default:
                System.out.println("\n✘ Invalid choice. Please enter 1 or 2.");
        }
 
        scanner.close();
    }
 
    // ── Mode 1: Generate & Save ──────────────────────────────────────────────
 
    private static void generateMode(Scanner scanner) {
        System.out.println("\n[ GENERATE HASH ]");
        System.out.print("Enter file path: ");
        String path = scanner.nextLine().trim();
 
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("\n✘ File not found.");
            return;
        }
 
        try {
            String hash = generateSHA256(file);
            System.out.println("\nSHA-256:");
            System.out.println("  " + hash);
 
            saveHash(file.getName(), hash);
            System.out.println("\n✔ Hash saved to " + HASH_STORE);
 
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("\n✘ Error: " + e.getMessage());
        }
    }
 
    // ── Mode 2: Verify ───────────────────────────────────────────────────────
 
    private static void verifyMode(Scanner scanner) {
        System.out.println("\n[ VERIFY FILE ]");
        System.out.print("Enter file path: ");
        String path = scanner.nextLine().trim();
 
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("\n✘ File not found.");
            return;
        }
 
        try {
            String currentHash = generateSHA256(file);
            String savedHash   = loadHash(file.getName());
 
            if (savedHash == null) {
                System.out.println("\n✘ No saved hash found for: " + file.getName());
                System.out.println("  Generate a hash first using option 1.");
                return;
            }
 
            System.out.println("\nCurrent Hash : " + currentHash);
            System.out.println("Saved Hash   : " + savedHash);
 
            if (currentHash.equals(savedHash)) {
                System.out.println("\n✔ File is UNCHANGED. Integrity verified.");
            } else {
                System.out.println("\n⚠ WARNING!");
                System.out.println("  File has been MODIFIED.");
            }
 
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("\n✘ Error: " + e.getMessage());
        }
    }
 
    // ── Utilities ────────────────────────────────────────────────────────────
 
    public static String generateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
 
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
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
 
    /**
     * Saves (or updates) the hash for a given filename in hashes.txt.
     * If the file already has an entry, it is overwritten.
     */
    private static void saveHash(String fileName, String hash) throws IOException {
        File store = new File(HASH_STORE);
        StringBuilder existing = new StringBuilder();
        boolean found = false;
 
        if (store.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(store))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String name    = line;
                    String oldHash = reader.readLine();
                    String sep     = reader.readLine(); // "---"
 
                    if (name != null && name.equals(fileName)) {
                        // Overwrite with new hash
                        existing.append(name).append("\n")
                                .append(hash).append("\n")
                                .append("---").append("\n");
                        found = true;
                    } else if (name != null && oldHash != null) {
                        existing.append(name).append("\n")
                                .append(oldHash).append("\n")
                                .append("---").append("\n");
                    }
                }
            }
        }
 
        if (!found) {
            existing.append(fileName).append("\n")
                    .append(hash).append("\n")
                    .append("---").append("\n");
        }
 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(store, false))) {
            writer.write(existing.toString());
        }
    }
 
    /** Returns the saved hash for a filename, or null if not found. */
    private static String loadHash(String fileName) throws IOException {
        File store = new File(HASH_STORE);
        if (!store.exists()) return null;
 
        try (BufferedReader reader = new BufferedReader(new FileReader(store))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String savedName = line;
                String savedHash = reader.readLine();
                reader.readLine(); // skip "---"
 
                if (savedName != null && savedName.equals(fileName)) {
                    return savedHash;
                }
            }
        }
        return null;
    }
}

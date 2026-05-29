import java.util.Scanner;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
/**
 * FileIntegrityChecker - Version 2
 * Features:
 *   - Generate SHA-256 hash of a file
 *   - Display hash on screen
 *   - Save hash to hashes.txt
 *
 * Author: Diya
 * Version: 2.0
 */
public class FileIntegrityCheckerV2 {
 
    private static final String HASH_STORE = "hashes.txt";
 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
 
        System.out.println("=============================");
        System.out.println("  FILE INTEGRITY CHECKER V2  ");
        System.out.println("=============================");
        System.out.println();
        System.out.print("Enter file path: ");
        String path = scanner.nextLine().trim();
 
        File file = new File(path);
 
        if (!file.exists()) {
            System.out.println("\n✘ File not found.");
            scanner.close();
            return;
        }
 
        System.out.println("\n✔ File exists!");
        System.out.println("  Name : " + file.getName());
        System.out.println("  Size : " + file.length() + " bytes");
 
        try {
            String hash = generateSHA256(file);
            System.out.println("\nSHA-256:");
            System.out.println("  " + hash);
 
            saveHash(file.getName(), hash);
            System.out.println("\n✔ Hash saved to " + HASH_STORE);
 
        } catch (IOException e) {
            System.out.println("\n✘ Error reading file: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("\n✘ SHA-256 algorithm not available.");
        }
 
        scanner.close();
    }
 
    /**
     * Reads the file in 8KB chunks and feeds each chunk into MessageDigest.
     * Returns the final SHA-256 hash as a lowercase hex string.
     */
    public static String generateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
 
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192]; // 8KB chunks
            int bytesRead;
 
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
 
        // Convert byte array to hex string
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
 
        return hexString.toString();
    }
 
    /**
     * Appends the filename and its hash to hashes.txt.
     * Format:
     *   filename
     *   sha256hash
     *   ---
     */
    private static void saveHash(String fileName, String hash) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HASH_STORE, true))) {
            writer.write(fileName);
            writer.newLine();
            writer.write(hash);
            writer.newLine();
            writer.write("---");
            writer.newLine();
        }
    }
}

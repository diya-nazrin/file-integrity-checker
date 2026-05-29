import java.util.Scanner;
import java.io.File;
 
/**
 * FileIntegrityChecker - Version 1
 * Task: Ask for a file path and check if the file exists.
 *
 * Author: Diya
 * Version: 1.0
 */
public class FileIntegrityCheckerV1 {
 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
 
        System.out.println("=============================");
        System.out.println("  FILE INTEGRITY CHECKER V1  ");
        System.out.println("=============================");
        System.out.println();
        System.out.print("Enter file path: ");
        String path = scanner.nextLine().trim();
 
        File file = new File(path);
 
        if (file.exists()) {
            System.out.println();
            System.out.println("✔ File exists!");
            System.out.println("  Name : " + file.getName());
            System.out.println("  Size : " + file.length() + " bytes");
        } else {
            System.out.println();
            System.out.println("✘ File not found.");
        }
 
        scanner.close();
    }
}

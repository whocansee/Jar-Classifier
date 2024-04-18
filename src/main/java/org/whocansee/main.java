/**
 * Author：whocansee
 * Date：2024-04-20
 */
package org.whocansee;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.*;
import java.util.Enumeration;


public class main {

    public static void main(String[] args) {
        System.out.println("      _                  ____ _               _  __ _           ");
        System.out.println("     | | __ _ _ __      / ___| | __ _ ___ ___(_)/ _(_) ___ _ __ ");
        System.out.println("  _  | |/ _` | '__|____| |   | |/ _` / __/ __| | |_| |/ _ \\ '__|");
        System.out.println(" | |_| | (_| | | |_____| |___| | (_| \\__ \\__ \\ |  _| |  __/ |   ");
        System.out.println("  \\___/ \\__,_|_|        \\____|_|\\__,_|___/___/_|_| |_|\\___|_|   ");
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter source folder path:");
        String sourceFolderPath = scanner.nextLine().trim();

        System.out.println("Enter destination folder path:");
        String destinationFolderPath = scanner.nextLine().trim();

        System.out.println("Enter keyword:");
        String keyword = scanner.nextLine().trim();

        scanner.close(); // 关闭 Scanner

        // 检查输入是否完整
        if (sourceFolderPath.isEmpty() || destinationFolderPath.isEmpty() || keyword.isEmpty()) {
            System.out.println("Usage: Please provide source folder path, destination folder path, and keyword.");
        }

        try {
            File sourceFolder = new File(sourceFolderPath);
            File destinationFolder = new File(destinationFolderPath);

            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            // List all files in the source folder
            File[] jarFiles = sourceFolder.listFiles((dir, name) -> name.endsWith(".jar"));

            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    processJarFile(jarFile, destinationFolder, keyword);
                }
            } else {
                System.out.println("No jar files found in the source folder.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Classification completed.");
    }

    private static void processJarFile(File jarFile, File destinationFolder, String keyword) throws IOException {
        String jarFileName = jarFile.getName();
        String jarNameWithoutExtension = jarFileName.substring(0, jarFileName.lastIndexOf("."));

        try (JarFile jar = new JarFile(jarFile)) {
            // Check if META-INF/maven folder exists
            JarEntry mavenFolderEntry = jar.getJarEntry("META-INF/maven");
            if (mavenFolderEntry == null) {
                // Move the jar file to unknown folder if maven folder not found
                moveJarFile(jarFile, destinationFolder.toPath().resolve("unknown"));
                return;
            }

            // Extract maven folder (if needed)
            // No need to extract, just proceed with further checks if necessary

            // Check pom.properties in META-INF/maven folder
            if (containsKeywordInMavenFolder(jar, keyword)) {
                // Move the jar file to keyword_jars folder if keyword found
                moveJarFile(jarFile, destinationFolder.toPath().resolve(keyword + "_jars"));
            } else {
                // Move the jar file to common folder if keyword not found
                moveJarFile(jarFile, destinationFolder.toPath().resolve("common"));
            }
        }
    }

    private static boolean containsKeywordInMavenFolder(JarFile jar, String keyword) {
        // Check if any pom.properties file in META-INF/maven contains the keyword
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith("META-INF/maven") && entry.getName().endsWith("/pom.properties")) {
                try (InputStream inputStream = jar.getInputStream(entry)) {
                    Properties props = new Properties();
                    props.load(inputStream);
                    // Check if properties contain the keyword
                    if (props.toString().contains(keyword)) {
                        return true;
                    }
                } catch (IOException e) {
                    // Handle IOException if necessary
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static void moveJarFile(File jarFile, Path destinationPath) throws IOException {
        if (!destinationPath.toFile().exists()) {
            destinationPath.toFile().mkdirs();
        }
        Files.copy(jarFile.toPath(), destinationPath.resolve(jarFile.getName()), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Copied " + jarFile.getName() + " to " + destinationPath);
    }
}

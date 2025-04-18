/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package haveno.common.file;

import com.google.common.io.Files;
import haveno.common.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class FileUtil {

    private static final String BACKUP_DIR = "backup";
    
    public static void rollingBackup(File dir, String fileName, int numMaxBackupFiles) {
        if (numMaxBackupFiles <= 0) return;
        if (dir.exists()) {
            File backupDir = new File(Paths.get(dir.getAbsolutePath(), BACKUP_DIR).toString());
            if (!backupDir.exists())
                if (!backupDir.mkdir())
                    log.warn("make dir failed.\nBackupDir=" + backupDir.getAbsolutePath());

            File origFile = new File(Paths.get(dir.getAbsolutePath(), fileName).toString());
            if (origFile.exists()) {
                String dirName = "backups_" + fileName;
                if (dirName.contains("."))
                    dirName = dirName.replace(".", "_");
                File backupFileDir = new File(Paths.get(backupDir.getAbsolutePath(), dirName).toString());
                if (!backupFileDir.exists())
                    if (!backupFileDir.mkdir())
                        log.warn("make backupFileDir failed.\nBackupFileDir=" + backupFileDir.getAbsolutePath());

                File backupFile = new File(Paths.get(backupFileDir.getAbsolutePath(), new Date().getTime() + "_" + fileName).toString());

                try {
                    Files.copy(origFile, backupFile);

                    pruneBackup(backupFileDir, numMaxBackupFiles);
                } catch (IOException e) {
                    log.error("Backup key failed: {}\n", e.getMessage(), e);
                }
            }
        }
    }

    public static List<File> getBackupFiles(File dir, String fileName) {
        File backupDir = new File(Paths.get(dir.getAbsolutePath(), BACKUP_DIR).toString());
        if (!backupDir.exists()) return new ArrayList<File>();
        String dirName = "backups_" + fileName;
        if (dirName.contains(".")) dirName = dirName.replace(".", "_");
        File backupFileDir = new File(Paths.get(backupDir.getAbsolutePath(), dirName).toString());
        if (!backupFileDir.exists()) return new ArrayList<File>();
        File[] files = backupFileDir.listFiles();
        return Arrays.asList(files);
    }

    public static File getLatestBackupFile(File dir, String fileName) {
        List<File> files = getBackupFiles(dir, fileName);
        if (files.isEmpty()) return null;
        files.sort(Comparator.comparing(File::getName));
        return files.get(files.size() - 1);
    }

    public static void deleteRollingBackup(File dir, String fileName) {
        File backupDir = new File(Paths.get(dir.getAbsolutePath(), BACKUP_DIR).toString());
        if (!backupDir.exists()) return;
        String dirName = "backups_" + fileName;
        if (dirName.contains(".")) dirName = dirName.replace(".", "_");
        File backupFileDir = new File(Paths.get(backupDir.getAbsolutePath(), dirName).toString());
        try {
            FileUtils.deleteDirectory(backupFileDir);
        } catch (IOException e) {
            log.error("Delete backup key failed: {}\n", e.getMessage(), e);
        }
    }

    private static void pruneBackup(File backupDir, int numMaxBackupFiles) {
        if (backupDir.isDirectory()) {
            File[] files = backupDir.listFiles();
            if (files != null) {
                List<File> filesList = Arrays.asList(files);
                if (filesList.size() > numMaxBackupFiles) {
                    filesList.sort(Comparator.comparing(File::getName));
                    File file = filesList.get(0);
                    if (file.isFile()) {
                        if (!file.delete())
                            log.error("Failed to delete file: " + file);
                        else
                            pruneBackup(backupDir, numMaxBackupFiles);

                    } else {
                        pruneBackup(new File(Paths.get(backupDir.getAbsolutePath(), file.getName()).toString()), numMaxBackupFiles);
                    }
                }
            }
        }
    }

    public static void deleteDirectory(File file) throws IOException {
        deleteDirectory(file, null, true);
    }

    public static void deleteDirectory(File file,
                                       @Nullable File exclude,
                                       boolean ignoreLockedFiles) throws IOException {
        boolean excludeFileFound = false;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null)
                for (File f : files) {
                    boolean excludeFileFoundLocal = exclude != null && f.getAbsolutePath().equals(exclude.getAbsolutePath());
                    excludeFileFound |= excludeFileFoundLocal;
                    if (!excludeFileFoundLocal)
                        deleteDirectory(f, exclude, ignoreLockedFiles);
                }
        }
        // Finally delete main file/dir if exclude file was not found in directory
        if (!excludeFileFound && !(exclude != null && file.getAbsolutePath().equals(exclude.getAbsolutePath()))) {
            try {
                deleteFileIfExists(file, ignoreLockedFiles);
            } catch (Throwable t) {
                log.error("Could not delete file. Error=" + t.toString());
                throw new IOException(t);
            }
        }
    }

    public static void deleteFileIfExists(File file) throws IOException {
        deleteFileIfExists(file, true);
    }

    public static void deleteFileIfExists(File file, boolean ignoreLockedFiles) throws IOException {
        try {
            if (Utilities.isWindows())
                file = file.getCanonicalFile();

            if (file.exists() && !file.delete()) {
                if (ignoreLockedFiles) {
                    // We check if file is locked. On Windows all open files are locked by the OS, so we
                    if (isFileLocked(file))
                        log.info("Failed to delete locked file: " + file.getAbsolutePath());
                } else {
                    final String message = "Failed to delete file: " + file.getAbsolutePath();
                    log.error(message);
                    throw new IOException(message);
                }
            }
        } catch (Throwable t) {
            log.error("Could not delete file, error={}\n", t.getMessage(), t);
            throw new IOException(t);
        }
    }

    private static boolean isFileLocked(File file) {
        return !file.canWrite();
    }

    public static void resourceToFile(String resourcePath,
                                      File destinationFile) throws ResourceNotFoundException, IOException {
        try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException(resourcePath);
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
                IOUtils.copy(inputStream, fileOutputStream);
            }
        }
    }

    public static boolean resourceEqualToFile(String resourcePath,
                                      File destinationFile) throws ResourceNotFoundException, IOException {
        try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException(resourcePath);
            }
            return IOUtils.contentEquals(inputStream, new FileInputStream(destinationFile));
        }
    }

    public static void renameFile(File oldFile, File newFile) throws IOException {
        if (Utilities.isWindows()) {
            // Work around an issue on Windows whereby you can't rename over existing files.
            final File canonical = newFile.getCanonicalFile();
            if (canonical.exists() && !canonical.delete()) {
                throw new IOException("Failed to delete canonical file for replacement with save");
            }
            if (!oldFile.renameTo(canonical)) {
                throw new IOException("Failed to rename " + oldFile + " to " + canonical);
            }
        } else if (!oldFile.renameTo(newFile)) {
            throw new IOException("Failed to rename " + oldFile + " to " + newFile);
        }
    }

    public static void copyFile(File origin, File target) throws IOException {
        if (!origin.exists()) {
            return;
        }

        try {
            Files.copy(origin, target);
        } catch (IOException e) {
            log.error("Copy file failed", e);
            throw new IOException("Failed to copy " + origin + " to " + target);
        }

    }

    public static void copyDirectory(File source, File destination) throws IOException {
        FileUtils.copyDirectory(source, destination);
    }

    public static File createNewFile(Path path) throws IOException {
        File file = path.toFile();
        if (!file.createNewFile()) {
            throw new IOException("There already exists a file with path: " + path);
        }
        return file;
    }

    public static void removeAndBackupFile(File dbDir, File storageFile, String fileName, String backupFolderName)
            throws IOException {
        File corruptedBackupDir = new File(Paths.get(dbDir.getAbsolutePath(), backupFolderName).toString());
        if (!corruptedBackupDir.exists() && !corruptedBackupDir.mkdir()) {
            log.warn("make dir failed");
        }

        File corruptedFile = new File(Paths.get(dbDir.getAbsolutePath(), backupFolderName, fileName).toString());
        if (storageFile.exists()) {
            renameFile(storageFile, corruptedFile);
        }
    }

    public static boolean doesFileContainKeyword(File file, String keyword) throws FileNotFoundException {
        Scanner s = new Scanner(file);
        while (s.hasNextLine()) {
            if (s.nextLine().contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

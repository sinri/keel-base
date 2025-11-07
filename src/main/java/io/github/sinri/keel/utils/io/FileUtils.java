package io.github.sinri.keel.utils.io;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class FileUtils {
    private FileUtils() {
    }

    /**
     * Checks if a file exists at the specified path.
     *
     * @param filePath the path to check
     * @return true if the file exists, false otherwise
     * @since 4.0.12
     */
    public static boolean exists(String filePath) {
        return Files.exists(Path.of(filePath));
    }

    /**
     * Creates a directory and all necessary parent directories.
     *
     * @param dirPath the directory path to create
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void mkdirs(String dirPath) throws IOException {
        Files.createDirectories(Path.of(dirPath));
    }

    /**
     * Creates a directory and all necessary parent directories asynchronously.
     *
     * @param dirPath the directory path to create
     * @return Future that completes when the directory is created
     * @since 4.0.12
     */
    public static Future<Void> mkdirsAsync(String dirPath) {
        return Keel.getVertx().executeBlocking(() -> {
            mkdirs(dirPath);
            return null;
        });
    }

    /**
     * Deletes a file or directory.
     *
     * @param path the path to delete
     * @throws IOException if an I/O error occurs
     * @since 4.1.0
     */
    public static void delete(String path) throws IOException {
        Files.deleteIfExists(Path.of(path));
    }

    /**
     * Deletes a file or directory asynchronously.
     *
     * @param path the path to delete
     * @return Future that completes when the deletion is done
     * @since 4.1.0
     */
    public static Future<Void> deleteAsync(String path) {
        return Keel.getVertx().executeBlocking(() -> {
            delete(path);
            return null;
        });
    }

    /**
     * Deletes a file or directory recursively.
     *
     * @param path the path to delete
     * @throws IOException if an I/O error occurs
     * @since 4.1.0
     */
    public static void deleteRecursive(String path) throws IOException {
        Path targetPath = Path.of(path);
        if (Files.exists(targetPath)) {
            Files.walkFileTree(targetPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Deletes a file or directory recursively asynchronously.
     *
     * @param path the path to delete
     * @return Future that completes when the deletion is done
     * @since 4.1.0
     */
    public static Future<Void> deleteRecursiveAsync(String path) {
        return Keel.getVertx().executeBlocking(() -> {
            deleteRecursive(path);
            return null;
        });
    }

    /**
     * Copies a file from source to destination.
     *
     * @param source      the source file path
     * @param destination the destination file path
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void copy(String source, String destination) throws IOException {
        Files.copy(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies a file from source to destination asynchronously.
     *
     * @param source      the source file path
     * @param destination the destination file path
     * @return Future that completes when the copy is done
     * @since 4.0.12
     */
    public static Future<Void> copyAsync(String source, String destination) {
        return Keel.getVertx().executeBlocking(() -> {
            copy(source, destination);
            return null;
        });
    }

    /**
     * Moves a file from source to destination.
     *
     * @param source      the source file path
     * @param destination the destination file path
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void move(String source, String destination) throws IOException {
        Files.move(Path.of(source), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Moves a file from source to destination asynchronously.
     *
     * @param source      the source file path
     * @param destination the destination file path
     * @return Future that completes when the move is done
     * @since 4.0.12
     */
    public static Future<Void> moveAsync(String source, String destination) {
        return Keel.getVertx().executeBlocking(() -> {
            move(source, destination);
            return null;
        });
    }

    public static @Nonnull byte[] readFileAsByteArray(@Nonnull String filePath, boolean seekInsideJarWhenNotFound) throws IOException {
        if (!isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            if (!seekInsideJarWhenNotFound) {
                throw new IOException("Failed to read file: " + filePath, e);
            }

            try (InputStream resourceAsStream = FileUtils.class.getClassLoader().getResourceAsStream(filePath)) {
                if (resourceAsStream == null) {
                    throw new IOException("File not found in filesystem or JAR: " + filePath, e);
                }
                return resourceAsStream.readAllBytes();
            } catch (IOException ex) {
                throw new IOException("Failed to read file from JAR: " + filePath, ex);
            }
        }
    }

    /**
     * @param filePath path string of the target file, or directory
     * @return the URL of target file; if not there, null return.
     * @since 3.2.12.1 original name is `getUrlOfFileInJar`.
     */
    @Nullable
    public static URL getUrlOfFileInRunningJar(@Nonnull String filePath) {
        return FileUtils.class.getClassLoader().getResource(filePath);
    }

    /**
     * Seek in JAR, under the root (exclusive)
     *
     * @param root ends with '/' for a directory
     * @return list of JarEntry
     * @since 3.2.12.1 original name is `traversalInJar`.
     */
    @Nonnull
    public static List<JarEntry> traversalInRunningJar(@Nonnull String root) {
        List<JarEntry> jarEntryList = new ArrayList<>();
        try {
            // should root ends with '/'?
            URL url = FileUtils.class.getClassLoader().getResource(root);
            if (url == null) {
                throw new RuntimeException("Resource is not found");
            }
            if (!url.toString().contains("!/")) {
                throw new RuntimeException("Resource is not in JAR");
            }
            String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2);

            URL jarURL = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            var baseJarEntry = jarFile.getJarEntry(root);
            var pathOfBaseJarEntry = Path.of(baseJarEntry.getName());

            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();

                Path entryPath = Path.of(entry.getName());
                if (entryPath.getParent() == null) {
                    continue;
                }
                if (entryPath.getParent().compareTo(pathOfBaseJarEntry) == 0) {
                    jarEntryList.add(entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jarEntryList;
    }

    /**
     * Creates a temporary file with the specified prefix and suffix.
     *
     * @param prefix the prefix for the temporary file name
     * @param suffix the suffix for the temporary file name
     * @return the absolute path of the created temporary file
     * @throws IOException if an I/O error occurs
     * @since 3.0.0
     */
    public static String createTempFile(@Nullable String prefix, @Nullable String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        return tempFile.toAbsolutePath().toString();
    }

    /**
     * Creates a temporary file with the specified prefix and suffix asynchronously.
     *
     * @param prefix the prefix for the temporary file name
     * @param suffix the suffix for the temporary file name
     * @return Future containing the absolute path of the created temporary file
     * @since 3.0.0
     */
    public static Future<String> createTempFileAsync(@Nullable String prefix, @Nullable String suffix) {
        return Keel.getVertx().executeBlocking(() -> createTempFile(prefix, suffix));
    }

    /**
     * @since 3.2.11
     * @since 3.2.12.1 Changed the implementation with checking class paths.
     *         Check if this process is running with JAR file.
     */
    public static boolean isRunningFromJAR() {
        List<String> classPathList = getClassPathList();
        for (var classPath : classPathList) {
            if (!classPath.endsWith(".jar")) {
                return false;
            }
        }
        return true;
    }

    /**
     * @since 3.2.12.1
     */
    public static List<String> getClassPathList() {
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        return new ArrayList<>(Arrays.asList(classpathEntries));
    }

    /**
     * The in-class classes, i.e. subclasses, would be neglected.
     *
     * @since 3.2.12.1 original name is `seekPackageClassFilesInJar`.
     */
    public static Set<String> seekPackageClassFilesInRunningJar(@Nonnull String packageName) {
        Set<String> classes = new HashSet<>();
        // Get the current class's class loader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Get the URL of the JAR file containing the current class
        String currentClassUrlInJarFile = FileUtils.class.getName().replace('.', '/') + ".class";
        URL jarUrl = classLoader.getResource(currentClassUrlInJarFile);

        if (jarUrl != null && jarUrl.getProtocol().equals("jar")) {
            // Extract the JAR file path
            String jarPath = jarUrl.getPath().substring(5, jarUrl.getPath().indexOf("!"));

            // Open the JAR file
            try (JarFile jarFile = new JarFile(jarPath)) {
                // Iterate through the entries of the JAR file
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    // Check if the entry is a class
                    if (entryName.endsWith(".class")) {
                        // Convert the entry name to a fully qualified class name
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        if (className.startsWith(packageName + ".") && !className.contains("$")) {
                            classes.add(className);
                        }
                    }
                }
            } catch (IOException e) {
                // Keel.getLogger().debug(FileUtils.class + " seekPackageClassFilesInRunningJar for package " + packageName + " error: " + e.getMessage());
            }
        }

        return classes;
    }

    /**
     * @param jarFile File built from JAR in class path.
     * @since 3.2.12.1
     */
    public static List<String> traversalInJarFile(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            List<String> list = new ArrayList<>();

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (
                        entryName.endsWith(".class")
                                && !entryName.contains("$")
                                && !entryName.startsWith("META-INF")
                ) {
                    // 将路径形式的类名转换为 Java 类名
                    String className = entryName.replace("/", ".").replace(".class", "");
                    list.add(className);
                }
            }

            return list;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param dirPath the directory path to list
     * @return list of file names in the directory
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static List<String> listDir(String dirPath) throws IOException {
        List<String> fileNames = new ArrayList<>();
        try (var stream = Files.list(Path.of(dirPath))) {
            stream.forEach(path -> fileNames.add(path.getFileName().toString()));
        }
        return fileNames;
    }


    /**
     * Creates a symbolic link.
     *
     * @param link   the path of the symbolic link to create
     * @param target the target of the symbolic link
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedOperationException if the operation is not supported
     * @since 4.0.12
     */
    public static void createSymLink(String link, String target) throws IOException {
        Files.createSymbolicLink(Path.of(link), Path.of(target));
    }

    /**
     * Creates a symbolic link asynchronously.
     *
     * @param link   the path of the symbolic link to create
     * @param target the target of the symbolic link
     * @return Future that completes when the link is created
     * @since 4.0.12
     */
    public static Future<Void> createSymLinkAsync(String link, String target) {
        return Keel.getVertx().executeBlocking(() -> {
            createSymLink(link, target);
            return null;
        });
    }

    /**
     * Reads a file as a string with a specific charset.
     *
     * @param filePath the file path
     * @param charset  the charset to use
     * @return the file contents as a string
     * @throws IllegalArgumentException if charset is invalid
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static String readFileAsString(String filePath, String charset) throws IOException {
        try {
            Charset charsetObj = Charset.forName(charset);
            return Files.readString(Path.of(filePath), charsetObj);
        } catch (java.nio.charset.IllegalCharsetNameException | java.nio.charset.UnsupportedCharsetException e) {
            throw new IllegalArgumentException("Invalid charset: " + charset, e);
        }
    }

    /**
     * Reads a file as a string with a specific charset asynchronously.
     *
     * @param filePath the file path
     * @param charset  the charset to use
     * @return Future containing the file contents as a string
     * @since 4.0.12
     */
    public static Future<String> readFileAsStringAsync(String filePath, String charset) {
        return Keel.getVertx().executeBlocking(() -> readFileAsString(filePath, charset));
    }

    /**
     * Writes a string to a file with a specific charset.
     *
     * @param filePath the file path
     * @param content  the content to write
     * @param charset  the charset to use
     * @throws IllegalArgumentException if charset is invalid
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void writeFile(String filePath, String content, String charset) throws IOException {
        try {
            writeFile(filePath, content, Charset.forName(charset));
        } catch (java.nio.charset.IllegalCharsetNameException | java.nio.charset.UnsupportedCharsetException e) {
            throw new IllegalArgumentException("Invalid charset: " + charset, e);
        }
    }

    /**
     * Writes a string to a file with a specific charset.
     *
     * @param filePath the file path
     * @param content  the content to write
     * @param charset  the charset to use
     * @throws IOException if an I/O error occurs
     * @since 4.1.0
     */
    public static void writeFile(String filePath, String content, Charset charset) throws IOException {
        Files.writeString(Path.of(filePath), content, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    /**
     * Writes a string to a file with a specific charset asynchronously.
     *
     * @param filePath the file path
     * @param content  the content to write
     * @param charset  the charset to use
     * @return Future that completes when the write is done
     * @since 4.0.12
     */
    public static Future<Void> writeFileAsync(String filePath, String content, String charset) {
        return Keel.getVertx().executeBlocking(() -> {
            writeFile(filePath, content, charset);
            return null;
        });
    }

    /**
     * Writes a string to a file with a specific charset asynchronously.
     *
     * @param filePath the file path
     * @param content  the content to write
     * @param charset  the charset to use
     * @return Future that completes when the write is done
     * @since 4.1.0
     */
    public static Future<Void> writeFileAsync(String filePath, String content, Charset charset) {
        return Keel.getVertx().executeBlocking(() -> {
            writeFile(filePath, content, charset);
            return null;
        });
    }

    /**
     * Appends content to a file.
     *
     * @param filePath the file path
     * @param content  the content to append
     * @throws IllegalArgumentException if filePath is null or empty
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void appendFile(String filePath, String content) throws IOException {
        appendFile(filePath, content, Charset.defaultCharset());
    }

    /**
     * Appends content to a file with a specific charset.
     *
     * @param filePath the file path
     * @param content  the content to append
     * @param charset  the charset to use
     * @throws IllegalArgumentException if filePath is null or empty or charset is invalid
     * @throws IOException if an I/O error occurs
     * @since 4.1.0
     */
    public static void appendFile(String filePath, String content, String charset) throws IOException {
        try {
            appendFile(filePath, content, Charset.forName(charset));
        } catch (java.nio.charset.IllegalCharsetNameException | java.nio.charset.UnsupportedCharsetException e) {
            throw new IllegalArgumentException("Invalid charset: " + charset, e);
        }
    }

    /**
     * Appends content to a file with a specific charset.
     *
     * @param filePath the file path
     * @param content  the content to append
     * @param charset  the charset to use
     * @throws IllegalArgumentException if filePath is null or empty
     * @throws IOException if an I/O error occurs
     * @since 4.1.0
     */
    public static void appendFile(String filePath, String content, Charset charset) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        Files.writeString(Path.of(filePath), content, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }

    /**
     * Appends content to a file asynchronously.
     *
     * @param filePath the file path
     * @param content  the content to append
     * @return Future that completes when the append is done
     * @since 4.0.12
     */
    public static Future<Void> appendFileAsync(String filePath, String content) {
        return Keel.getVertx().executeBlocking(() -> {
            appendFile(filePath, content);
            return null;
        });
    }

    /**
     * Appends content to a file with a specific charset asynchronously.
     *
     * @param filePath the file path
     * @param content  the content to append
     * @param charset  the charset to use
     * @return Future that completes when the append is done
     * @since 4.1.0
     */
    public static Future<Void> appendFileAsync(String filePath, String content, String charset) {
        return Keel.getVertx().executeBlocking(() -> {
            appendFile(filePath, content, charset);
            return null;
        });
    }

    /**
     * Appends content to a file with a specific charset asynchronously.
     *
     * @param filePath the file path
     * @param content  the content to append
     * @param charset  the charset to use
     * @return Future that completes when the append is done
     * @since 4.1.0
     */
    public static Future<Void> appendFileAsync(String filePath, String content, Charset charset) {
        return Keel.getVertx().executeBlocking(() -> {
            appendFile(filePath, content, charset);
            return null;
        });
    }

    /**
     * Extracts a JAR file to a directory.
     *
     * @param jarPath   the path to the JAR file
     * @param targetDir the target directory
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void extractJar(String jarPath, String targetDir) throws IOException {
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                File file = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (InputStream in = jar.getInputStream(entry);
                         java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                        in.transferTo(out);
                    }
                }
            }
        }
    }

    /**
     * Extracts a JAR file to a directory asynchronously.
     *
     * @param jarPath   the path to the JAR file
     * @param targetDir the target directory
     * @return Future that completes when the extraction is done
     * @since 4.0.12
     */
    public static Future<Void> extractJarAsync(String jarPath, String targetDir) {
        return Keel.getVertx().executeBlocking(() -> {
            extractJar(jarPath, targetDir);
            return null;
        });
    }

    /**
     * Creates a new JAR file from a directory.
     *
     * @param sourceDir the source directory
     * @param jarPath   the path where the JAR file will be created
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void createJar(String sourceDir, String jarPath) throws IOException {
        try (java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(
                new java.io.FileOutputStream(jarPath))) {
            File source = new File(sourceDir);
            addToJar(source, source, jos);
        }
    }

    /**
     * Creates a new JAR file from a directory asynchronously.
     *
     * @param sourceDir the source directory
     * @param jarPath   the path where the JAR file will be created
     * @return Future that completes when the JAR is created
     * @since 4.0.12
     */
    public static Future<Void> createJarAsync(String sourceDir, String jarPath) {
        return Keel.getVertx().executeBlocking(() -> {
            createJar(sourceDir, jarPath);
            return null;
        });
    }

    private static void addToJar(File root, File source, java.util.jar.JarOutputStream jos) throws IOException {
        String normalizedPath = source.getPath().substring(root.getPath().length() + 1).replace('\\', '/');
        if (source.isDirectory()) {
            String dirPath = normalizedPath;
            if (!dirPath.isEmpty()) {
                if (!dirPath.endsWith("/")) {
                    dirPath += "/";
                }
                JarEntry entry = new JarEntry(dirPath);
                jos.putNextEntry(entry);
                jos.closeEntry();
            }
            var files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    addToJar(root, file, jos);
                }
            }
        } else {
            String entryPath = normalizedPath;
            JarEntry entry = new JarEntry(entryPath);
            jos.putNextEntry(entry);
            try (java.io.FileInputStream fis = new java.io.FileInputStream(source)) {
                fis.transferTo(jos);
            }
            jos.closeEntry();
        }
    }

    /**
     * Gets the size of a file.
     *
     * @param filePath the file path
     * @return the file size in bytes
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static long getFileSize(String filePath) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(Path.of(filePath), BasicFileAttributes.class);
        return attrs.size();
    }

    /**
     * Checks if a path is a directory.
     *
     * @param path the path to check
     * @return true if the path is a directory
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static boolean isDirectory(String path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(Path.of(path), BasicFileAttributes.class);
        return attrs.isDirectory();
    }

    /**
     * Gets the last modified time of a file.
     *
     * @param filePath the file path
     * @return the last modified time in milliseconds since epoch
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static long getLastModifiedTime(String filePath) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(Path.of(filePath), BasicFileAttributes.class);
        return attrs.lastModifiedTime().toMillis();
    }

    /**
     * Gets the creation time of a file.
     *
     * @param filePath the file path
     * @return the creation time in milliseconds since epoch
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static long getCreatedTime(String filePath) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(Path.of(filePath), BasicFileAttributes.class);
        return attrs.creationTime().toMillis();
    }

    /**
     * Creates a ZIP file from a directory or file.
     *
     * @param sourcePath the source file or directory path
     * @param zipPath    the path where the ZIP file will be created
     * @throws IllegalArgumentException if sourcePath or zipPath is null or empty
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void createZip(String sourcePath, String zipPath) throws IOException {
        if (!isValidPath(sourcePath)) {
            throw new IllegalArgumentException("Source path cannot be null or empty");
        }
        if (!isValidPath(zipPath)) {
            throw new IllegalArgumentException("ZIP path cannot be null or empty");
        }
        File source = new File(sourcePath);
        if (!source.exists()) {
            throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
        }
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                new java.io.FileOutputStream(zipPath))) {
            addToZip(source, source, zos);
        }
    }

    /**
     * Creates a ZIP file from a directory or file asynchronously.
     *
     * @param sourcePath the source file or directory path
     * @param zipPath    the path where the ZIP file will be created
     * @return Future that completes when the ZIP is created
     * @since 4.0.12
     */
    public static Future<Void> createZipAsync(String sourcePath, String zipPath) {
        return Keel.getVertx().executeBlocking(() -> {
            createZip(sourcePath, zipPath);
            return null;
        });
    }

    /**
     * Extracts a ZIP file to a directory.
     *
     * @param zipPath   the path to the ZIP file
     * @param targetDir the target directory
     * @throws IllegalArgumentException if zipPath or targetDir is null or empty
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void extractZip(String zipPath, String targetDir) throws IOException {
        if (!isValidPath(zipPath)) {
            throw new IllegalArgumentException("ZIP path cannot be null or empty");
        }
        if (!isValidPath(targetDir)) {
            throw new IllegalArgumentException("Target directory cannot be null or empty");
        }
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) {
            throw new IllegalArgumentException("ZIP file does not exist: " + zipPath);
        }
        File baseDir = new File(targetDir);
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipPath)) {
            Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                File file = resolvePath(baseDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!file.mkdirs() && !file.exists()) {
                        throw new IOException("Failed to create directory: " + file.getPath());
                    }
                } else {
                    file.getParentFile().mkdirs();
                    try (InputStream in = zip.getInputStream(entry);
                         java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                        in.transferTo(out);
                    }
                }
            }
        }
    }

    /**
     * Extracts a ZIP file to a directory asynchronously.
     *
     * @param zipPath   the path to the ZIP file
     * @param targetDir the target directory
     * @return Future that completes when the extraction is done
     * @since 4.0.12
     */
    public static Future<Void> extractZipAsync(String zipPath, String targetDir) {
        return Keel.getVertx().executeBlocking(() -> {
            extractZip(zipPath, targetDir);
            return null;
        });
    }

    private static void addToZip(File root, File source, java.util.zip.ZipOutputStream zos) throws IOException {
        String normalizedPath = source.getPath().substring(root.getPath().length() + 1).replace('\\', '/');
        if (source.isDirectory()) {
            String dirPath = normalizedPath;
            if (!dirPath.isEmpty()) {
                if (!dirPath.endsWith("/")) {
                    dirPath += "/";
                }
                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(dirPath);
                zos.putNextEntry(entry);
                zos.closeEntry();
            }
            var files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    addToZip(root, file, zos);
                }
            }
        } else {
            String entryPath = normalizedPath;
            java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryPath);
            zos.putNextEntry(entry);
            try (java.io.FileInputStream fis = new java.io.FileInputStream(source)) {
                fis.transferTo(zos);
            }
            zos.closeEntry();
        }
    }

    /**
     * Lists the contents of a ZIP file.
     *
     * @param zipPath the path to the ZIP file
     * @return list of entry names in the ZIP file
     * @throws IllegalArgumentException if zipPath is null or empty
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static List<String> listZipContents(String zipPath) throws IOException {
        if (!isValidPath(zipPath)) {
            throw new IllegalArgumentException("ZIP path cannot be null or empty");
        }
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) {
            throw new IllegalArgumentException("ZIP file does not exist: " + zipPath);
        }
        List<String> entries = new ArrayList<>();
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipPath)) {
            Enumeration<? extends java.util.zip.ZipEntry> zipEntries = zip.entries();
            while (zipEntries.hasMoreElements()) {
                entries.add(zipEntries.nextElement().getName());
            }
        }
        return entries;
    }

    /**
     * Extracts a specific file from a ZIP archive.
     *
     * @param zipPath    the path to the ZIP file
     * @param entryName  the name of the entry to extract
     * @param targetPath the path where the file will be extracted
     * @throws IllegalArgumentException if any parameter is null or empty
     * @throws IOException if an I/O error occurs
     * @since 4.0.12
     */
    public static void extractZipEntry(String zipPath, String entryName, String targetPath) throws IOException {
        if (!isValidPath(zipPath)) {
            throw new IllegalArgumentException("ZIP path cannot be null or empty");
        }
        if (!isValidPath(entryName)) {
            throw new IllegalArgumentException("Entry name cannot be null or empty");
        }
        if (!isValidPath(targetPath)) {
            throw new IllegalArgumentException("Target path cannot be null or empty");
        }
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) {
            throw new IllegalArgumentException("ZIP file does not exist: " + zipPath);
        }
        File baseDir = new File(targetPath).getParentFile();
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipPath)) {
            java.util.zip.ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                throw new IOException("Entry not found: " + entryName);
            }
            File targetFile = resolvePath(baseDir, new File(targetPath).getName());
            if (!targetFile.getParentFile().mkdirs() && !targetFile.getParentFile().exists()) {
                throw new IOException("Failed to create parent directory: " + targetFile.getParent());
            }
            try (InputStream in = zip.getInputStream(entry);
                 java.io.FileOutputStream out = new java.io.FileOutputStream(targetFile)) {
                in.transferTo(out);
            }
        }
    }

    /**
     * Extracts a specific file from a ZIP archive asynchronously.
     *
     * @param zipPath    the path to the ZIP file
     * @param entryName  the name of the entry to extract
     * @param targetPath the path where the file will be extracted
     * @return Future that completes when the extraction is done
     * @since 4.0.12
     */
    public static Future<Void> extractZipEntryAsync(String zipPath, String entryName, String targetPath) {
        return Keel.getVertx().executeBlocking(() -> {
            extractZipEntry(zipPath, entryName, targetPath);
            return null;
        });
    }

    /**
     * Validates a file path for security concerns.
     * <p>
     * Checks for: <br>
     * - Null or empty paths <br>
     * - Null bytes <br>
     * - Path traversal attempts using ".." <br>
     * - Maximum path length <br>
     * - Invalid characters <br>
     * <p>
     * Examples: <br>
     * isValidPath("file.txt") -> true <br>
     * isValidPath("dir/file.txt") -> true <br>
     * isValidPath("../file.txt") -> false <br>
     * isValidPath("file?.txt") -> false <br>
     * isValidPath(null) -> false <br>
     * isValidPath("") -> false <br>
     *
     * @param path the path to validate
     * @return true if the path is valid, false otherwise
     * @since 4.0.12
     */
    private static boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        // Check for null bytes
        if (path.contains("\0")) {
            return false;
        }

        // Normalize path
        Path normalizedPath;
        try {
            normalizedPath = Path.of(path).normalize();
        } catch (Exception e) {
            return false;
        }

        // Check for path traversal attempts
        if (normalizedPath.toString().contains("..")) {
            return false;
        }

        // Check for maximum path length
        if (normalizedPath.toString().length() > 4096) { // Common max path length
            return false;
        }

        // Check for invalid characters
        String invalidChars = "<>:\"|?*";
        for (char c : invalidChars.toCharArray()) {
            if (path.indexOf(c) != -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Safely resolves a path against a base directory to prevent path traversal attacks.
     *
     * @param baseDir the base directory
     * @param path    the path to resolve
     * @return the resolved path
     * @throws IllegalArgumentException if the resolved path is outside the base directory
     * @since 4.0.12
     */
    private static File resolvePath(File baseDir, String path) {
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        try {
            // Convert to absolute paths
            File baseDirAbs = baseDir.getAbsoluteFile();
            File resolved = new File(baseDirAbs, path).getAbsoluteFile();

            // Check for symbolic links
            Path basePath = baseDirAbs.toPath();
            Path resolvedPath = resolved.toPath();

            // Resolve any symbolic links
            Path realBasePath = basePath.toRealPath();
            Path realResolvedPath = resolvedPath.toRealPath();

            // Check if the resolved path is within the base directory
            if (!realResolvedPath.startsWith(realBasePath)) {
                throw new IllegalArgumentException("Path traversal attempt detected: " + path);
            }

            return resolved;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve path: " + path, e);
        }
    }
}


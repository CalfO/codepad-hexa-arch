package fr.cbtw.interview.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ClasspathScanner {
    private ClasspathScanner() {
    }

    /** Retourne toutes les classes chargeables trouvées dans le package donné, y compris dans ses sous-packages (scan récursif). */
    public static List<Class<?>> findClassesInPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClasspathScanner.class.getClassLoader();
        }
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                    classes.addAll(scanDirectory(directory, packageName));
                } else if ("jar".equals(resource.getProtocol())) {
                    classes.addAll(scanJar(resource, path));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de scanner le package " + packageName, e);
        }
        return classes;
    }

    private static List<Class<?>> scanDirectory(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(scanDirectory(file, packageName + "." + file.getName()));
                continue;
            }
            if (file.getName().endsWith(".class")) {
                String simpleName = file.getName().substring(0, file.getName().length() - 6);
                String className = packageName + "." + simpleName;
                loadClass(className).ifPresent(classes::add);
            }
        }
        return classes;
    }

    private static List<Class<?>> scanJar(URL resource, String path) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            JarURLConnection connection = (JarURLConnection) resource.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    boolean isClassUnderPackage = name.startsWith(path + "/") && name.endsWith(".class");
                    if (isClassUnderPackage) {
                        String className = name.replace('/', '.').substring(0, name.length() - 6);
                        loadClass(className).ifPresent(classes::add);
                    }
                }
            }
        } catch (IOException | ClassCastException e) {
            throw new IllegalStateException("Impossible de scanner le jar pour " + path, e);
        }
        return classes;
    }

    private static Optional<Class<?>> loadClass(String className) {
        try {
            return Optional.of(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }
}

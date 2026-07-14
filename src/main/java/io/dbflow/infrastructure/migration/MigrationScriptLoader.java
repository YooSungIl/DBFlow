package io.dbflow.infrastructure.migration;

import io.dbflow.common.exception.ServiceException;
import io.dbflow.domain.MigrationScript;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationScriptLoader {

    private static final String MIGRATION_DIRECTORY = "db/migration";
    private static final Pattern SCRIPT_NAME_PATTERN = Pattern.compile(
            "^V(\\d+)\\.(\\d+)\\.(\\d+)__.+\\.sql$"
    );

    public List<MigrationScript> load() {
        try {
            URL directoryUrl = getClass().getClassLoader().getResource(MIGRATION_DIRECTORY);
            if (directoryUrl == null) {
                throw new ServiceException(ServiceException.MIGRATION_SCRIPT_LOAD_FAILED);
            }

            List<String> resourceNames = switch (directoryUrl.getProtocol()) {
                case "file" -> findFileResources(directoryUrl.toURI());
                case "jar" -> findJarResources(directoryUrl);
                default -> throw new ServiceException(ServiceException.MIGRATION_SCRIPT_LOAD_FAILED);
            };

            List<MigrationScript> scripts = new ArrayList<>();
            for (String resourceName : resourceNames) {
                MigrationScript script = loadScript(resourceName);
                if (script != null) {
                    scripts.add(script);
                }
            }
            return scripts;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(ServiceException.MIGRATION_SCRIPT_LOAD_FAILED, e);
        }
    }

    private List<String> findFileResources(URI directoryUri) throws Exception {
        List<String> resources = new ArrayList<>();
        try (var paths = Files.list(Path.of(directoryUri))) {
            paths.filter(Files::isRegularFile)
                    .map(path -> MIGRATION_DIRECTORY + "/" + path.getFileName())
                    .forEach(resources::add);
        }
        return resources;
    }

    private List<String> findJarResources(URL directoryUrl) throws Exception {
        List<String> resources = new ArrayList<>();
        JarURLConnection connection = (JarURLConnection) directoryUrl.openConnection();
        try (JarFile jarFile = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if (entryName.startsWith(MIGRATION_DIRECTORY + "/") && !entryName.endsWith("/")) {
                    resources.add(entryName);
                }
            }
        }
        return resources;
    }

    private MigrationScript loadScript(String resourceName) throws Exception {
        String scriptName = Path.of(resourceName).getFileName().toString();
        Matcher matcher = SCRIPT_NAME_PATTERN.matcher(scriptName);
        if (!matcher.matches()) {
            return null;
        }

        byte[] content;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new ServiceException(ServiceException.MIGRATION_SCRIPT_LOAD_FAILED);
            }
            content = inputStream.readAllBytes();
        }

        return new MigrationScript(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                scriptName,
                sha256(content),
                new String(content, StandardCharsets.UTF_8)
        );
    }

    private String sha256(byte[] content) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
        return java.util.HexFormat.of().formatHex(digest);
    }
}

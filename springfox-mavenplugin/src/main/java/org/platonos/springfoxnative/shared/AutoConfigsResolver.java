package org.platonos.springfoxnative.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AutoConfigsResolver {

    private AutoConfigsResolver() {
    }

    public static List<URL> resolve(final List<File> files) {
        return files.stream()
                .map(AutoConfigsResolver::resolveAutoConfigurations)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private static List<URL> resolveAutoConfigurations(final File dependencyFile) {
        final List<URL> autoConfigs = new ArrayList<>();

        try(final ZipFile zipFile = new ZipFile(dependencyFile)) {
            final ZipEntry zipEntry = zipFile.getEntry("META-INF/spring.factories");

            if (zipEntry != null) {
                final InputStream inputStream = zipFile.getInputStream(zipEntry);
                final Properties properties = new Properties();
                properties.load(inputStream);
                final String configFiles = properties.getProperty("org.springframework.boot.autoconfigure.EnableAutoConfiguration");

                if (configFiles != null) {
                    final ClassLoader classLoader = AutoConfigsResolver.class.getClassLoader();

                    final List<URL> configLocations = Arrays.stream(configFiles.split(","))
                            .map(configFile -> configFile.replace('.', '/') + ".class")
                            .map(classLoader::getResource)
                            .collect(Collectors.toList());
                    autoConfigs.addAll(configLocations);
                }
            }
        } catch (final IOException e) {
            //Ignore
        }

        return autoConfigs;
    }
}

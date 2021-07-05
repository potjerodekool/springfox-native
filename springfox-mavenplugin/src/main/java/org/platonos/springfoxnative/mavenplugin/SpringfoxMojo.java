package org.platonos.springfoxnative.mavenplugin;

import io.swagger.models.Swagger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.objectweb.asm.ClassReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import springfox.documentation.service.Documentation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.swagger2.mappers.*;
import springfox.documentation.swagger2.web.SwaggerTransformationContext;
import springfox.documentation.swagger2.web.WebMvcBasePathAndHostnameTransformationFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.HttpServletRequest;

@Mojo(name = "dependency-counter", defaultPhase = LifecyclePhase.COMPILE)
public class SpringfoxMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    private Set<Artifact> getDependencyArtifacts() {
        final Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
        return dependencyArtifacts.stream()
                .filter(artifact -> artifact.getGroupId().startsWith("org.springframework") ||
                        artifact.getGroupId().startsWith("io.springfox")).collect(Collectors.toSet());
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Set<Artifact> dependencyArtifacts = getDependencyArtifacts();

        final List<URL> autoConfigs = new ArrayList<>();

        dependencyArtifacts.stream()
                .map(Artifact::getFile)
                .filter(file -> file.getName().endsWith(".jar"))
                .forEach(file -> resolveAutoConfigurations(file, autoConfigs));

        final List<org.platonos.springfoxnative.mavenplugin.ClassDefinition> classDefinitions = autoConfigs.stream()
                .map(this::readClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        final org.platonos.springfoxnative.mavenplugin.Environment environment = new org.platonos.springfoxnative.mavenplugin.Environment();

        final List<Class> classes = new ArrayList<>();

        classes.addAll(
            classDefinitions.stream()
                    .filter(classDefinition -> classDefinition.isEnabled(environment))
                    .map(org.platonos.springfoxnative.mavenplugin.ClassDefinition::loadClass)
                    .filter(Objects::nonNull).collect(Collectors.toList())
        );

        DefaultListableBeanFactory defaultListableBeanFactory = new DefaultListableBeanFactory();

        final Docket docket = new Docket(DocumentationType.SWAGGER_2);
        defaultListableBeanFactory.registerSingleton("docket", docket);

        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(defaultListableBeanFactory);

        for (final Class<?> componentClass : classes) {
            applicationContext.register(componentClass);
        }

        applicationContext.refresh();

        final DocumentationPluginsBootstrapper documentationPluginsBootstrapper = applicationContext.getBean(DocumentationPluginsBootstrapper.class);

        final Map<String, Documentation> documentations = documentationPluginsBootstrapper.getScanned().all();

        final ServiceModelToSwagger2MapperImpl mapper = applicationContext.getBean(ServiceModelToSwagger2MapperImpl.class);

        final JsonSerializer serializer = new JsonSerializer(new ArrayList<>());
        org.springframework.core.env.Environment servletEnvironment = new StandardServletEnvironment();

        documentations.forEach((key, documentation) -> {
            Swagger swagger = mapper.mapDocumentation(documentation);
            swagger.setHost("$host");

            SwaggerTransformationContext context = createSwaggerTransformationContext(swagger, null);
            final WebMvcBasePathAndHostnameTransformationFilter filter = new WebMvcBasePathAndHostnameTransformationFilter(servletEnvironment);
            context = context.next(filter.transform(context));
            swagger = context.getSpecification();
            final Json json = serializer.toJson(swagger);
            System.out.println(json.value());
        });
    }

    private SwaggerTransformationContext<HttpServletRequest> createSwaggerTransformationContext(final Swagger swagger,
                                                                                                final HttpServletRequest request) {
        try {
            Constructor<SwaggerTransformationContext> constructor = SwaggerTransformationContext.class.getDeclaredConstructor(Swagger.class, Object.class);
            constructor.setAccessible(true);
            return constructor.newInstance(swagger, request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void resolveAutoConfigurations(final File dependencyFile, List<URL> autoConfigs) {
        try(ZipFile zipFile = new ZipFile(dependencyFile)) {
            ZipEntry zipEntry = zipFile.getEntry("META-INF/spring.factories");

            if (zipEntry != null) {
                final InputStream inputStream = zipFile.getInputStream(zipEntry);
                final Properties properties = new Properties();
                properties.load(inputStream);
                final String configFiles = properties.getProperty("org.springframework.boot.autoconfigure.EnableAutoConfiguration");

                if (configFiles != null) {
                    final ClassLoader classLoader = getClass().getClassLoader();

                    final List<URL> configLocations = Arrays.stream(configFiles.split(","))
                            .map(configFile -> configFile.replace('.', '/') + ".class")
                            .map(classLoader::getResource)
                            .collect(Collectors.toList());
                    autoConfigs.addAll(configLocations);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private org.platonos.springfoxnative.mavenplugin.ClassDefinition readClass(final URL url) {
        try {
            byte[] bytecode = url.openStream().readAllBytes();
            final ClassReader classReader = new ClassReader(bytecode);
            final org.platonos.springfoxnative.mavenplugin.ComponentDetectorVisitor visitor = new org.platonos.springfoxnative.mavenplugin.ComponentDetectorVisitor();
            classReader.accept(visitor, ClassReader.SKIP_CODE);
            return visitor.getClassDefinition();
        } catch (final IOException e) {
            //Ignore
            return null;
        }
    }

}

package org.platonos.springfoxnative.shared;

import io.swagger.models.Swagger;
import org.objectweb.asm.ClassReader;
import org.platonos.springfoxnative.shared.asm.visitor.TypeElementVisitor;
import org.platonos.springfoxnative.shared.element.TypeElement;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.StandardServletEnvironment;
import springfox.documentation.service.Documentation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;
import springfox.documentation.swagger2.web.SwaggerTransformationContext;
import springfox.documentation.swagger2.web.WebMvcBasePathAndHostnameTransformationFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpringFox {

    public void execute(final List<File> dependencyFiles) {
        final List<URL> autoConfigs = AutoConfigsResolver.resolve(dependencyFiles);

        final List<TypeElement> typeElements = autoConfigs.stream()
                .map(this::readClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        final Environment environment = new SpringEnvironment();

        final List<Class<?>> classes = typeElements.stream()
                .filter(typeElement -> typeElement.isEnabled(environment))
                .map(TypeElement::loadClass)
                .filter(Objects::nonNull).collect(Collectors.toList());

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

            SwaggerTransformationContext<HttpServletRequest> context = createSwaggerTransformationContext(swagger, null);
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

    private TypeElement readClass(final URL url) {
        try {
            byte[] bytecode = url.openStream().readAllBytes();
            final ClassReader classReader = new ClassReader(bytecode);
            final TypeElementVisitor visitor = new TypeElementVisitor();
            classReader.accept(visitor, ClassReader.SKIP_CODE);
            return visitor.getClassDefinition();
        } catch (final IOException e) {
            //Ignore
            return null;
        }
    }
}

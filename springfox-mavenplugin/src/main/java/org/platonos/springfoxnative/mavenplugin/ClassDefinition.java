package org.platonos.springfoxnative.mavenplugin;

import org.objectweb.asm.Type;
import org.platonos.springfoxnative.mavenplugin.annotationvalue.AnnotationValue;
import org.platonos.springfoxnative.mavenplugin.annotationvalue.ArrayAnnotationValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class ClassDefinition {

    private String className;
    private final Map<String, List<org.platonos.springfoxnative.mavenplugin.Annotation>> annotationsMap = new HashMap<>();

    public void setClassName(final String className) {
        this.className = className;
    }

    public void addAnnotation(final String annotationClassName,
                              final org.platonos.springfoxnative.mavenplugin.Annotation annotation) {
        final List<org.platonos.springfoxnative.mavenplugin.Annotation> list = annotationsMap.computeIfAbsent(annotationClassName, key -> new ArrayList<>());
        list.add(annotation);
    }

    public boolean isEnabled(final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        boolean enabled = true;

        final List<org.platonos.springfoxnative.mavenplugin.Annotation> annotations = annotationsMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (final org.platonos.springfoxnative.mavenplugin.Annotation annotation : annotations) {
            final String annotationClassName = annotation.getAnnotationClassName();

            switch (annotationClassName) {
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty" : {
                    if (!conditionalOnProperty(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnClass" : {
                    if (!conditionalOnClass(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnBean" : {
                    if (!conditionalOnBean(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean": {
                    if (!conditionalOnMissingBean(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.context.annotation.Conditional": {
                    if (!conditional(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType": {
                    if (!conditionalOnRepositoryType(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate": {
                    if (!conditionalOnSingleCandidate(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication": {
                    if (!conditionalOnWebApplication(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnResource": {
                    if (!conditionalOnResource(annotation, environment)) {
                        enabled = false;
                    }
                    break;
                }
                default: {
                    if (annotationClassName.toLowerCase().contains("conditional")) {
                        throw new UnsupportedOperationException("unhanled condition annotation " + annotationClassName);
                    }
                }
            }
        }

        return enabled;
    }

    private boolean conditionalOnProperty(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                          final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        final ConditionalOnProperty conditionalOnProperty = createAnnotationProxy(annotation, ConditionalOnProperty.class);

        boolean match = true;

        final String prefix = conditionalOnProperty.prefix();
        final String[] value = getOrDefault(conditionalOnProperty.value(), conditionalOnProperty.name());
        final String havingValue = conditionalOnProperty.havingValue();
        final boolean matchIfMissing = conditionalOnProperty.matchIfMissing();

        for (int i = 0; i < value.length; i++) {
            final String propertyName;

            if (prefix != null) {
                if (prefix.endsWith(".")) {
                    propertyName = prefix + value[i];
                } else {
                    propertyName = prefix + "." + value[i];
                }
            } else {
                propertyName = value[i];
            }


            if (environment.hasProperty(propertyName)) {
                final String propertyValue = environment.getPropertyValue(propertyName);
                if (!havingValue.equals(propertyValue)) {
                    match = false;
                }
            } else if (!matchIfMissing) {
                match = false;
            }
        }

        return match;
    }

    private boolean conditionalOnClass(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                       final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        final ArrayAnnotationValue value = (ArrayAnnotationValue) annotation.getAnnotationValue("value");
        final List<Type> values = (List<Type>) value.getValue();

        final List<String> classNames = values.stream()
                .map(Type::getClassName)
                .collect(Collectors.toList());

        return classNames.stream()
                .allMatch(className -> isClassPresentOnClassPath(className));
    }

    //TODO
    private boolean conditionalOnBean(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                      final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnMissingBean(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                             final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        return false;
    }

    //TODO
    private boolean conditional(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnRepositoryType(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                                final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnSingleCandidate(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                                 final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnWebApplication(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                                final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnResource(final org.platonos.springfoxnative.mavenplugin.Annotation annotation,
                                          final org.platonos.springfoxnative.mavenplugin.Environment environment) {
        return false;
    }

    private boolean isClassPresentOnClassPath(final String className) {
        try {
            getClass().getClassLoader().loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private <A> A createAnnotationProxy(final org.platonos.springfoxnative.mavenplugin.Annotation annotation, final Class<A> annotationClass) {
        final ClassLoader loader = getClass().getClassLoader();
        return (A) Proxy.newProxyInstance(
                loader,
                new Class[]{annotationClass},
                new AnnotationInvocationHandler(annotation)
        );
    }

    private <E> E getOrDefault(E value, E defaultValue) {
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public Class<?> loadClass() {
        try {
            return getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}

class AnnotationInvocationHandler implements InvocationHandler {

    private final org.platonos.springfoxnative.mavenplugin.Annotation annotation;

    public AnnotationInvocationHandler(final org.platonos.springfoxnative.mavenplugin.Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String methodName = method.getName();

        Class<?> returnType = method.getReturnType();
        AnnotationValue annotationValue = annotation.getAnnotationValue(methodName);

        if (annotationValue == null) {
            if (returnType.isPrimitive()) {
                return getDefaultValueForPrimitive(returnType);
            }

            return null;
        } else {
            return unpackValue(annotationValue.getValue(), returnType);
        }
    }

    //TODO use default value of annotation.
    private Object getDefaultValueForPrimitive(final Class<?> returnType) {
        if (returnType == Boolean.TYPE) {
            return false;
        } else {
            throw new UnsupportedOperationException("" + returnType);
        }
    }

    private Object unpackValue(Object value, Class<?> returnType) {
        if (returnType.isArray()) {
            final List<Object> values = (List<Object>) value;

            final Class<?> componentType = returnType.getComponentType();
            final Object[] array = (Object[]) Array.newInstance(componentType, values.size());

            for (int i = 0; i < values.size(); i++) {
                array[i] = values.get(i);
            }

            return array;
        } else {
            return value;
        }
    }

}
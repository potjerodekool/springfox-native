package org.platonos.springfoxnative.shared.element;

import org.objectweb.asm.Type;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class TypeElement {

    private String className;
    private final Map<String, List<AnnotationMirror>> annotationsMap = new HashMap<>();

    public void setClassName(final String className) {
        this.className = className;
    }

    public void addAnnotation(final String annotationClassName,
                              final AnnotationMirror annotationMirror) {
        final List<AnnotationMirror> list = annotationsMap.computeIfAbsent(annotationClassName, key -> new ArrayList<>());
        list.add(annotationMirror);
    }

    public boolean isEnabled(final Environment environment) {
        boolean enabled = true;

        final List<AnnotationMirror> annotationMirrors = annotationsMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (final AnnotationMirror annotationMirror : annotationMirrors) {
            final String annotationClassName = annotationMirror.getAnnotationClassName();

            switch (annotationClassName) {
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty" : {
                    if (!conditionalOnProperty(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnClass" : {
                    if (!conditionalOnClass(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnBean" : {
                    if (!conditionalOnBean(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean": {
                    if (!conditionalOnMissingBean(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.context.annotation.Conditional": {
                    if (!conditional(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType": {
                    if (!conditionalOnRepositoryType(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate": {
                    if (!conditionalOnSingleCandidate(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication": {
                    if (!conditionalOnWebApplication(annotationMirror, environment)) {
                        enabled = false;
                    }
                    break;
                }
                case "org.springframework.boot.autoconfigure.condition.ConditionalOnResource": {
                    if (!conditionalOnResource(annotationMirror, environment)) {
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

    private boolean conditionalOnProperty(final AnnotationMirror annotationMirror,
                                          final Environment environment) {
        final ConditionalOnProperty conditionalOnProperty = createAnnotationProxy(annotationMirror, ConditionalOnProperty.class);

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


            if (environment.containsProperty(propertyName)) {
                final String propertyValue = environment.getProperty(propertyName);
                if (!havingValue.equals(propertyValue)) {
                    match = false;
                }
            } else if (!matchIfMissing) {
                match = false;
            }
        }

        return match;
    }

    private boolean conditionalOnClass(final AnnotationMirror annotationMirror,
                                       final Environment environment) {
        final ArrayAnnotationValue value = (ArrayAnnotationValue) annotationMirror.getAnnotationValue("value");
        final List<Type> values = (List<Type>) value.getValue();

        final List<String> classNames = values.stream()
                .map(Type::getClassName)
                .collect(Collectors.toList());

        return classNames.stream()
                .allMatch(className -> isClassPresentOnClassPath(className));
    }

    //TODO
    private boolean conditionalOnBean(final AnnotationMirror annotationMirror,
                                      final Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnMissingBean(final AnnotationMirror annotationMirror,
                                             final Environment environment) {
        return false;
    }

    //TODO
    private boolean conditional(final AnnotationMirror annotationMirror,
                                final Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnRepositoryType(final AnnotationMirror annotationMirror,
                                                final Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnSingleCandidate(final AnnotationMirror annotationMirror,
                                                 final Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnWebApplication(final AnnotationMirror annotationMirror,
                                                final Environment environment) {
        return false;
    }

    //TODO
    private boolean conditionalOnResource(final AnnotationMirror annotationMirror,
                                          final Environment environment) {
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

    private <A> A createAnnotationProxy(final AnnotationMirror annotationMirror, final Class<A> annotationClass) {
        final ClassLoader loader = getClass().getClassLoader();
        return (A) Proxy.newProxyInstance(
                loader,
                new Class[]{annotationClass},
                new AnnotationInvocationHandler(annotationMirror)
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

    private final AnnotationMirror annotationMirror;

    public AnnotationInvocationHandler(final AnnotationMirror annotationMirror) {
        this.annotationMirror = annotationMirror;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final String methodName = method.getName();

        Class<?> returnType = method.getReturnType();
        AnnotationValue annotationValue = annotationMirror.getAnnotationValue(methodName);

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
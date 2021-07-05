package org.platonos.springfoxnative.mavenplugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.platonos.springfoxnative.mavenplugin.annotationvalue.ArrayAnnotationValue;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.util.*;

public class ComponentDetectorVisitor extends ClassVisitor {

    private static final List<String> COMPONENT_ANNOTATIONS =
            Arrays.asList(
                    "org.springframework.stereotype.Component",
                    "org.springframework.context.annotation.Configuration"
            );

    private List<String> annotationClasses = new ArrayList<>();
    private List<org.platonos.springfoxnative.mavenplugin.Annotation> annotations = new ArrayList<>();
    private Map<String, List<org.platonos.springfoxnative.mavenplugin.Annotation>> annotationsMap = new HashMap<>();

    private final org.platonos.springfoxnative.mavenplugin.ClassDefinition classDefinition;

    public ComponentDetectorVisitor() {
        super(Opcodes.ASM9);
        classDefinition = new org.platonos.springfoxnative.mavenplugin.ClassDefinition();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classDefinition.setClassName(name.replace('/', '.'));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        final String annotationClassName = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');

        final org.platonos.springfoxnative.mavenplugin.Annotation annotation = new org.platonos.springfoxnative.mavenplugin.Annotation(annotationClassName);
        annotations.add(annotation);

        final List<org.platonos.springfoxnative.mavenplugin.Annotation> list = annotationsMap.computeIfAbsent(annotationClassName, key -> new ArrayList<>());
        list.add(annotation);
        annotationClasses.add(annotationClassName);

        classDefinition.addAnnotation(annotationClassName, annotation);

        return new org.platonos.springfoxnative.mavenplugin.AnnotationVisitorImpl(api, annotation);
    }

    public org.platonos.springfoxnative.mavenplugin.ClassDefinition getClassDefinition() {
        return classDefinition;
    }

    private boolean checkConditional() {
        final List<org.platonos.springfoxnative.mavenplugin.Annotation> annotations =  annotationsMap.get("org.springframework.context.annotation.Conditional");

        if (annotations != null) {
            final ConditionContextImpl context = new ConditionContextImpl();

            for (org.platonos.springfoxnative.mavenplugin.Annotation annotation : annotations) {
                final ArrayAnnotationValue annotationValue = (ArrayAnnotationValue) annotation.getAnnotationValue("value");
                boolean matches = true;

                final List<Object> values = (List<Object>) annotationValue.getValue();

                for (Object value : values) {
                    Type conditionType = (Type) value;
                    final String conitionClassName = conditionType.getClassName();
                    final Condition condition = loadConditionClass(conitionClassName);
                    if (!condition.matches(context, null)) {
                        matches = false;
                    }
                }

                return matches;
            }

            return false;
        }

        return true;
    }

    private Condition loadConditionClass(final String className) {
        Condition condition;

        try {
            final Class<Condition> conditionClass = (Class<Condition>) getClass().getClassLoader().loadClass(className);
            condition = conditionClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            condition = (context, metadata) -> false;
        }

        return condition;
    }

}

class ConditionContextImpl implements ConditionContext {

    @Override
    public BeanDefinitionRegistry getRegistry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Environment getEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceLoader getResourceLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
package org.platonos.springfoxnative.mavenplugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ComponentDetectorVisitor extends ClassVisitor {

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

        final var annotation = new Annotation(annotationClassName);
        classDefinition.addAnnotation(annotationClassName, annotation);

        return new AnnotationVisitorImpl(api, annotation);
    }

    public ClassDefinition getClassDefinition() {
        return classDefinition;
    }

}

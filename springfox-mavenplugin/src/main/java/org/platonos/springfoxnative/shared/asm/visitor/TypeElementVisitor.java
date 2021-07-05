package org.platonos.springfoxnative.shared.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.platonos.springfoxnative.shared.element.AnnotationMirror;
import org.platonos.springfoxnative.shared.element.TypeElement;

public class TypeElementVisitor extends ClassVisitor {

    private final TypeElement typeElement;

    public TypeElementVisitor() {
        super(Opcodes.ASM9);
        typeElement = new TypeElement();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        typeElement.setClassName(name.replace('/', '.'));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        final String annotationClassName = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');

        final var annotation = new AnnotationMirror(annotationClassName);
        typeElement.addAnnotation(annotationClassName, annotation);

        return new AnnotationVisitorImpl(api, annotation);
    }

    public TypeElement getClassDefinition() {
        return typeElement;
    }

}

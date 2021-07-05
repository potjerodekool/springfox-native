package org.platonos.springfoxnative.mavenplugin;

import org.objectweb.asm.AnnotationVisitor;
import org.platonos.springfoxnative.mavenplugin.annotationvalue.ArrayAnnotationValue;
import org.platonos.springfoxnative.mavenplugin.annotationvalue.ConstantAnnotationValue;

import java.util.Objects;

public class AnnotationVisitorImpl extends AnnotationVisitor {

    private org.platonos.springfoxnative.mavenplugin.Annotation annotation;

    public AnnotationVisitorImpl(int api, org.platonos.springfoxnative.mavenplugin.Annotation annotation) {
        super(api);
        this.annotation = annotation;
    }

    @Override
    public void visit(String name, Object value) {
        Objects.requireNonNull(name);
        annotation.addAnnotationValue(name, new ConstantAnnotationValue(value));
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return super.visitAnnotation(name, descriptor);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        final ArrayAnnotationValue annotationValue = new ArrayAnnotationValue();
        annotation.addAnnotationValue(name, annotationValue);
        return new org.platonos.springfoxnative.mavenplugin.ArrayAnnotationVisitor(api, annotationValue);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
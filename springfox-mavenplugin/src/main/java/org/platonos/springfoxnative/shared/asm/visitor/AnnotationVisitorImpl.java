package org.platonos.springfoxnative.shared.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.platonos.springfoxnative.shared.element.ArrayAnnotationValue;
import org.platonos.springfoxnative.shared.element.ConstantAnnotationValue;
import org.platonos.springfoxnative.shared.element.AnnotationMirror;

import java.util.Objects;

public class AnnotationVisitorImpl extends AnnotationVisitor {

    private final AnnotationMirror annotationMirror;

    public AnnotationVisitorImpl(int api, AnnotationMirror annotationMirror) {
        super(api);
        this.annotationMirror = annotationMirror;
    }

    @Override
    public void visit(String name, Object value) {
        Objects.requireNonNull(name);
        annotationMirror.addAnnotationValue(name, new ConstantAnnotationValue(value));
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
        annotationMirror.addAnnotationValue(name, annotationValue);
        return new ArrayAnnotationVisitor(api, annotationValue);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
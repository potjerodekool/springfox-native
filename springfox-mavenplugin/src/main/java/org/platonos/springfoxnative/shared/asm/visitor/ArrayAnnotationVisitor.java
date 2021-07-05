package org.platonos.springfoxnative.shared.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.platonos.springfoxnative.shared.element.ArrayAnnotationValue;

public class ArrayAnnotationVisitor extends AnnotationVisitor {

    private final ArrayAnnotationValue annotationValue;

    public ArrayAnnotationVisitor(final int api,
                                  final ArrayAnnotationValue annotationValue) {
        super(api);
        this.annotationValue = annotationValue;
    }

    @Override
    public void visit(String name, Object value) {
        annotationValue.addValue(value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}

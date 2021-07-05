package org.platonos.springfoxnative.shared.element;

public class ConstantAnnotationValue implements AnnotationValue {

    private final Object value;

    public ConstantAnnotationValue(final Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }
}

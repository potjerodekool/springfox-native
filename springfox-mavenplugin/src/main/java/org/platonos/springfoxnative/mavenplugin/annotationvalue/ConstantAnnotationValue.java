package org.platonos.springfoxnative.mavenplugin.annotationvalue;

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

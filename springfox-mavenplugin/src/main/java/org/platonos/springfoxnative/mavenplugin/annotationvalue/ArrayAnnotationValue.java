package org.platonos.springfoxnative.mavenplugin.annotationvalue;

import java.util.ArrayList;
import java.util.List;

public class ArrayAnnotationValue implements AnnotationValue {

    private List<Object> values = new ArrayList<>();

    @Override
    public Object getValue() {
        return values;
    }

    public void addValue(final Object value) {
        this.values.add(value);
    }
}

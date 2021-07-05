package org.platonos.springfoxnative.shared.element;

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

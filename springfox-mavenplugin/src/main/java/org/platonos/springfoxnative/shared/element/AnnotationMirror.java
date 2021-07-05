package org.platonos.springfoxnative.shared.element;

import java.util.HashMap;
import java.util.Map;

public class AnnotationMirror {

    private final String annotationClassName;
    private final Map<String, AnnotationValue> annotationValues = new HashMap<>();

    public AnnotationMirror(final String annotationClassName) {
        this.annotationClassName = annotationClassName;
    }

    public String getAnnotationClassName() {
        return annotationClassName;
    }

    public AnnotationValue getAnnotationValue(final String name) {
        return annotationValues.get(name);
    }

    public void addAnnotationValue(final String name,
                                   final AnnotationValue annotationValue) {
        this.annotationValues.put(name, annotationValue);
    }
}

package com.rce.editor.structures;

import java.util.Objects;

public class Parameter {
    public String key;
    public String resolution;

    public Parameter() {
    }

    public Parameter(String key, String resolution) {
        this.key = key;
        this.resolution = resolution;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return key.equals(parameter.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}

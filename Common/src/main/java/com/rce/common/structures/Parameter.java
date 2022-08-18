package com.rce.common.structures;

import java.util.List;
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

    public String id() {
        return key.split("\\|")[1];
    }

    public String[] idSegments() {
        return id().split("\\.");
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

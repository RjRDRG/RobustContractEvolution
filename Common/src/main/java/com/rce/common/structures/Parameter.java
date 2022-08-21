package com.rce.common.structures;

import java.util.Objects;
import java.util.Optional;

public class Parameter {
    public enum Type {String, Number, Boolean, Object, Array}

    public String key;
    public String resolution;
    public Type type;

    public Parameter() {
    }

    public Parameter(String key, String resolution, Type type) {
        this.key = key;
        this.resolution = resolution;
        this.type = type;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String id() {
        String[] sa = key.split("\\|");
        if(sa.length>1)
            return sa[1];
        else
            return null;
    }

    public String[] idSegments() {
        return Optional.ofNullable(id()).map(a -> a.split("\\.")).orElse(new String[0]);
    }

    public String resolutionType() {
        return resolution.split("=")[0];
    }

    public String resolutionLinkType() {
        return resolution.split("=")[1].split("\\|")[0];
    }

    public String resolutionLinkId() {
        String[] s = resolution.split("=")[1].split("\\|");
        if(s.length>1)
            return s[1];
        else
            return null;
    }

    public String resolutionValue() {
        return resolution.split("=")[1];
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

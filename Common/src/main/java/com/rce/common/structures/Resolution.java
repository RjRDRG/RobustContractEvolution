package com.rce.common.structures;

import java.util.Objects;

public class Resolution {

    public enum Type {LINK, VALUE, FUNCTION}

    public static String TypeSeparator = "=";

    public final String resolution;

    private Resolution(String resolution) {
        this.resolution = resolution;
    }

    public static Resolution linkResolution(String key) {
        String resolution = Type.LINK.name().toLowerCase() + TypeSeparator  + key;
        return new Resolution(resolution);
    }

    public static Resolution valueResolution(String value) {
        String resolution = Type.VALUE.name().toLowerCase() + TypeSeparator + value;
        return new Resolution(resolution);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resolution that = (Resolution) o;
        return resolution.equals(that.resolution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolution);
    }
}

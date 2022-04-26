package com.rce.editor.structures;

import com.rce.parser.structures.PropertyKey;

import java.util.Objects;

public class Resolution {

    public static String LINK = "link=";
    public static String VALUE = "value=";
    public static String TYPE = "type=";

    public final String resolution;

    private Resolution(String resolution) {
        this.resolution = resolution;
    }

    public static Resolution linkResolution(PropertyKey key) {
        String resolution = LINK + key.toString();
        return new Resolution(resolution);
    }

    public static Resolution valueResolution(String value) {
        String resolution = VALUE + value;
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

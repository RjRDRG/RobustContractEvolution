package com.rce.parser.structures;

import java.util.List;
import java.util.Objects;

public class Property {
    public final PropertyKey key;
    public final boolean array;
    public final String primitive;
    public final String format;

    public final boolean required;
    public final String defaultValue;

    public boolean solved;

    public Property(PropertyKey.Location location, List<String> precursors, String name, boolean array, String primitive, String format, boolean required, String defaultValue) {
        this.key = new PropertyKey(location, precursors, name);
        this.array = array;
        this.primitive = primitive;
        this.format = format;
        this.required = required;
        this.defaultValue = defaultValue;
        this.solved = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return array == property.array && key.equals(property.key) && Objects.equals(primitive, property.primitive) && Objects.equals(format, property.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, array, primitive, format);
    }

    @Override
    public String toString() {
        return String.format(
                "key: %-40s | isArray: %-5s | primitive: %-10s | format: %-10s | required: %-5s | default: %s",
                key, array, primitive, format, required, defaultValue
        );
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public boolean isCompatible(Property property) {
        return Objects.equals(primitive,property.primitive) && Objects.equals(format,property.format);
    }
}

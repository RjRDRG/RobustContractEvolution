package com.rce.parser.structures;

import java.util.*;

public class PropertyKey {
    public enum Location {HEADER, PATH, QUERY, COOKIE, JSON}

    public final Location location;
    public final List<String> predecessors;
    public final String name;

    public PropertyKey(Location location, List<String> predecessors, String name) {
        this.location = location;
        this.predecessors = predecessors;
        this.name = name;
    }

    @Override
    public String toString() {
        List<String> p = new LinkedList<>(predecessors);
        if(name!=null)
            p.add(name);
        return location.name().toLowerCase() + "|"
                + String.join(".", p);
    }

    public PropertyKey fromString(String s1) {
        String[] s2 = s1.split("\\|");
        Location location = Location.valueOf(s2[0].toUpperCase());

        if(s2.length == 2) {
            String[] s3 = s2[1].split(".");
            String name = s3[s3.length - 1];

            List<String> predecessors;
            if (s3.length == 1) {
                predecessors = Collections.emptyList();
            } else {
                predecessors = Arrays.asList(s3).subList(0, s3.length - 2);
            }
            return new PropertyKey(location, predecessors, name);
        }
        else {
            return new PropertyKey(location, Collections.emptyList(), null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyKey that = (PropertyKey) o;
        return location == that.location && predecessors.equals(that.predecessors) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, predecessors, name);
    }
}

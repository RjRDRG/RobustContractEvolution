package com.rce.common.structures;

import java.util.Objects;

public class Endpoint {
    public enum Method {GET, PUT, POST, DELETE, PATCH, OPTIONS, HEAD, TRACE}

    public String path;
    public Method method;

    public Endpoint(String path, Method method) {
        this.path = path;
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endpoint)) return false;
        Endpoint that = (Endpoint) o;
        return cleanPath().equals(that.cleanPath()) && method == that.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cleanPath(), method);
    }

    public String cleanPath() {
        return path.replaceAll("\\{.*?\\}", "VAR");
    }

    @Override
    public String toString() {
        return path + " " + method;
    }

    public static Endpoint fromString(String s) {
        String[] ss = s.split(" ");
        return new Endpoint(ss[0], Method.valueOf(ss[1]));
    }


}

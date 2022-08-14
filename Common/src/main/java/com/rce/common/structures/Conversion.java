package com.rce.common.structures;

import java.util.LinkedList;
import java.util.List;

public class Conversion {
    List<Method> methods;

    public Conversion() {
        methods = new LinkedList<>();
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
}

package com.rce.editor.structures;

import java.util.LinkedList;
import java.util.List;

public class Result {
    List<Method> methods;

    public Result() {
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

package com.rce.common.structures;

import java.util.LinkedList;
import java.util.List;

public class ContractEvolution {
    String serviceName;
    String priorVersion;
    String version;
    List<Method> methods;

    public ContractEvolution(String serviceName, String priorVersion, String version) {
        this.serviceName = serviceName;
        this.priorVersion = priorVersion;
        this.version = version;
        methods = new LinkedList<>();
    }

    public ContractEvolution() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPriorVersion() {
        return priorVersion;
    }

    public void setPriorVersion(String priorVersion) {
        this.priorVersion = priorVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

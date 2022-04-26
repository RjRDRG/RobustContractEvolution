package com.rce.editor.structures;

import java.util.List;

public class Message {

    public final static String REQUEST = "request";
    public final static String RESPONSE = "response";

    public String type;
    public String typePrior;
    public List<Parameter> parameters;

    public Message() {
    }

    public Message(String type, String typePrior, List<Parameter> parameters) {
        this.type = type;
        this.typePrior = typePrior;
        this.parameters = parameters;
    }


    public static Message requestMessage(List<Parameter> parameters) {
        return new Message(REQUEST, REQUEST, parameters);
    }

    public static Message responseMessage(String status, String priorStatus, List<Parameter> parameters) {
        return new Message(RESPONSE + " " + status, RESPONSE + " " + priorStatus, parameters);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypePrior() {
        return typePrior;
    }

    public void setTypePrior(String typePrior) {
        this.typePrior = typePrior;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}

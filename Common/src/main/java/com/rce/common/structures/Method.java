package com.rce.common.structures;

import java.util.LinkedList;
import java.util.List;

public class Method {

    public String endpoint;
    public String endpointPrior;

    public List<Message> messages;

    public Method() {
    }

    public Method(String endpoint, String endpointPrior) {
        this.endpoint = endpoint;
        this.endpointPrior = endpointPrior;
        this.messages = new LinkedList<>();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpointPrior() {
        return endpointPrior;
    }

    public void setEndpointPrior(String endpointPrior) {
        this.endpointPrior = endpointPrior;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}

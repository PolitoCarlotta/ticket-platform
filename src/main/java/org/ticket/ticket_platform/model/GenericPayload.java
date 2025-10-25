package org.ticket.ticket_platform.model;

public class GenericPayload <T>{

    private T payload;

    private String message;

    private String statusCode;

    public GenericPayload(T payload, String message, String statusCode) {
        this.payload = payload;
        this.message = message;
        this.statusCode = statusCode;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    

}

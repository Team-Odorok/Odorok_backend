package com.odorok.OdorokApplication.commons.exception;

public class GptCommunicationException extends RuntimeException {
    public GptCommunicationException(String message) {
        super(message);
    }

    public GptCommunicationException(String message, Throwable cause) {
        super(message, cause); 
    }
}

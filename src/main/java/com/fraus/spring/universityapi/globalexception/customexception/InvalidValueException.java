package com.fraus.spring.universityapi.globalexception.customexception;

import lombok.Getter;

@Getter
public class InvalidValueException extends RuntimeException {

    private final String logDetails;

    public InvalidValueException(
            String userMessage,
            String logDetails
    ) {
        super(userMessage);
        this.logDetails = logDetails;
    }
}

package com.fraus.spring.universityapi.globalexception.customexception;

import lombok.Getter;

@Getter
public class AlreadyExistsException extends RuntimeException {

    private final String logDetails;

    public AlreadyExistsException(
            String userMessage,
            String logDetails
    ) {
        super(userMessage);
        this.logDetails = logDetails;
    }
}

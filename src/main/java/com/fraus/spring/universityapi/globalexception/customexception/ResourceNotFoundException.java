package com.fraus.spring.universityapi.globalexception.customexception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String logDetails;

    public ResourceNotFoundException(
            String userMessage,
            String logDetails
    ) {
        super(userMessage);
        this.logDetails = logDetails;
    }
}

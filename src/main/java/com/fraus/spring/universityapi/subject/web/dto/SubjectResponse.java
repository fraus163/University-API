package com.fraus.spring.universityapi.subject.web.dto;

public record SubjectResponse(
        Integer id,
        String name,
        String description
) {
}

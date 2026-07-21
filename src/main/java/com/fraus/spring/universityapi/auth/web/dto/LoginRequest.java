package com.fraus.spring.universityapi.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest (
        @NotBlank(message = "Email обязателен для ввода")
        @Email(message = "Некорректный формат Email")
        @Size(max = 30, message = "Email не должен превышать 30 символов")
        String email,

        @NotBlank(message = "Пароль обязателен для ввода")
        @Size(min = 6, max = 30, message = "Пароль не должен превышать 30 символов")
        String password
) {
}

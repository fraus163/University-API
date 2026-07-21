package com.fraus.spring.universityapi.auth.web.dto;

import jakarta.validation.constraints.*;

public record SignupRequest (
        @NotBlank(message = "Email обязателен для ввода")
        @Email(message = "Некорректный email")
        @Size(max = 30, message = "Email не должен превышать 30 символов")
        String email,

        @NotBlank(message = "Пароль обязателен для ввода")
        @Size(min = 6, max = 30, message = "Пароль должен содержать от 6 до 30 символов")
        String password,

        @NotBlank(message = "Фамилия обязательна для ввода")
        @Size(max = 30, message = "Фамилия не должна превышать 30 символов")
        String lastName,

        @NotBlank(message = "Имя обязательно для ввода")
        @Size(max = 30, message = "Имя не должно превышать 30 символов")
        String firstName,

        @Size(max = 30, message = "Отчество не должно превышать 30 символов")
        String patronymic,

        @NotBlank(message = "Номер телефона обязателен для ввода")
        @Pattern(
                regexp = "^(\\+7|8)?\\d{10}$",
                message = "Неверный формат номера телефона. Используйте +7XXXXXXXXXX или 8XXXXXXXXXX"
        )
        String phoneNumber
){
}

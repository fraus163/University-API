package com.fraus.spring.universityapi.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraus.spring.universityapi.auth.domain.AuthService;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.security.WebSecurityConfig;
import com.fraus.spring.universityapi.auth.web.dto.JwtResponse;
import com.fraus.spring.universityapi.auth.web.dto.LoginRequest;
import com.fraus.spring.universityapi.auth.web.dto.SignupRequest;
import com.fraus.spring.universityapi.globalexception.GlobalExceptionHandler;
import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, WebSecurityConfig.class})
@DisplayName("WebMvc-тестирование AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @Nested
    @DisplayName("POST /api/v1/auth/signin - Вход в систему")
    class SignIn {

        @Test
        @DisplayName("Должен успешно аутентифицировать и вернуть 200 OK с токеном")
        @WithAnonymousUser
        void shouldAuthenticateAndReturn200WithToken() throws Exception {
            LoginRequest request = new LoginRequest("user@university.com", "password123");
            JwtResponse response = new JwtResponse("mocked-jwt-token", "APPLICANT");

            when(authService.signIn(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/signin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jwtToken").value("mocked-jwt-token"))
                    .andExpect(jsonPath("$.role").value("APPLICANT"));

            verify(authService, times(1)).signIn(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 400 Bad Request при передаче некорректного email")
        @WithAnonymousUser
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            LoginRequest request = new LoginRequest("invalid-email", "password123");

            mockMvc.perform(post("/api/v1/auth/signin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).signIn(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/signup - Регистрация абитуриента")
    class SignUp {

        @Test
        @DisplayName("Должен успешно зарегистрировать пользователя и вернуть 200 OK")
        @WithAnonymousUser
        void shouldRegisterUserSuccessfully() throws Exception {
            SignupRequest request = new SignupRequest(
                    "newuser@university.com",
                    "password123",
                    "Иванов",
                    "Иван",
                    "Иванович",
                    "+79291234567"
            );

            doNothing().when(authService).signUp(any(SignupRequest.class));

            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authService, times(1)).signUp(any(SignupRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть 409 Conflict при попытке зарегистрировать уже существующий email")
        @WithAnonymousUser
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            SignupRequest request = new SignupRequest(
                    "existing@university.com",
                    "password123",
                    "Петров",
                    "Пётр",
                    "Петрович",
                    "+79297654321"
            );

            doThrow(new AlreadyExistsException("Email уже существует", "Email already exists: existing@university.com"))
                    .when(authService).signUp(any(SignupRequest.class));

            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Email уже существует"));
        }
    }
}
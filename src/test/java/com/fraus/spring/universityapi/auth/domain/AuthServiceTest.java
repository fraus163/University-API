package com.fraus.spring.universityapi.auth.domain;

import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.web.dto.JwtResponse;
import com.fraus.spring.universityapi.auth.web.dto.LoginRequest;
import com.fraus.spring.universityapi.auth.web.dto.SignupRequest;
import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Вход в систему (signIn)")
    class SignIn {

        @Test
        @DisplayName("Должен успешно аутентифицировать пользователя и вернуть JWT токен")
        void shouldAuthenticateAndReturnJwtToken() {
            LoginRequest loginRequest = new LoginRequest("user@university.com", "password123");
            Authentication authentication = mock(Authentication.class);

            doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .when(authentication).getAuthorities();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn("mocked-jwt-token");

            JwtResponse response = authService.signIn(loginRequest);

            assertThat(response).isNotNull();
            assertThat(response.jwtToken()).isEqualTo("mocked-jwt-token");
            assertThat(response.role()).isEqualTo("ADMIN");

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils, times(1)).generateJwtToken(authentication);
        }
    }

    @Nested
    @DisplayName("Регистрация пользователя (signUp)")
    class SignUp {

        @Test
        @DisplayName("Должен успешно зарегистрировать нового абитуриента")
        void shouldRegisterNewApplicantSuccessfully() {
            SignupRequest signupRequest = new SignupRequest(
                    "applicant@university.com",
                    "rawPassword",
                    "Иванов",
                    "Иван",
                    "Иванович",
                    "+375291234567"
            );

            when(userRepository.existsByEmail(signupRequest.email())).thenReturn(false);
            when(encoder.encode(signupRequest.password())).thenReturn("encodedPassword");

            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

            authService.signUp(signupRequest);

            verify(userRepository, times(1)).save(userCaptor.capture());
            UserEntity savedUser = userCaptor.getValue();

            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getEmail()).isEqualTo("applicant@university.com");
            assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.APPLICANT);
            assertThat(savedUser.getLastName()).isEqualTo("Иванов");
            assertThat(savedUser.getFirstName()).isEqualTo("Иван");
            assertThat(savedUser.getPatronymic()).isEqualTo("Иванович");
            assertThat(savedUser.getPhoneNumber()).isEqualTo("+375291234567");

            assertThat(savedUser.getApplicant()).isNotNull();
            assertThat(savedUser.getApplicant().getUser()).isEqualTo(savedUser);
        }

        @Test
        @DisplayName("Должен выбросить AlreadyExistsException, если email уже зарегистрирован")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            SignupRequest signupRequest = new SignupRequest(
                    "existing@university.com",
                    "password",
                    "Петров",
                    "Пётр",
                    "Петрович",
                    "+375297654321"
            );

            when(userRepository.existsByEmail(signupRequest.email())).thenReturn(true);

            assertThatThrownBy(() -> authService.signUp(signupRequest))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("Email уже существует");

            verify(userRepository, never()).save(any());
        }
    }
}
package com.fraus.spring.universityapi.auth.domain;

import com.fraus.spring.universityapi.applicant.domain.db.ApplicantEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.auth.security.JwtUtils;
import com.fraus.spring.universityapi.auth.web.dto.JwtResponse;
import com.fraus.spring.universityapi.auth.web.dto.LoginRequest;
import com.fraus.spring.universityapi.auth.web.dto.SignupRequest;
import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public JwtResponse signIn(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .orElse(UserRole.APPLICANT.name());

        return new JwtResponse(
                jwtUtils.generateJwtToken(authentication),
                role
        );
    }

    @Transactional
    public void signUp(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistsException(
                    "Email уже существует",
                    "Email already exists: email=" + request.email()
            );
        }

        UserEntity userToSave = new UserEntity();
        userToSave.setEmail(request.email());
        userToSave.setPassword(encoder.encode(request.password()));
        userToSave.setRole(UserRole.APPLICANT);
        userToSave.setLastName(request.lastName());
        userToSave.setFirstName(request.firstName());
        userToSave.setPatronymic(request.patronymic());
        userToSave.setPhoneNumber(request.phoneNumber());

        ApplicantEntity applicantToSave = new ApplicantEntity();
        applicantToSave.setUser(userToSave);
        userToSave.setApplicant(applicantToSave);

        userRepository.save(userToSave);
    }
}
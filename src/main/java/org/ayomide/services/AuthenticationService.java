package org.ayomide.services;

import org.ayomide.dto.request.LoginRequest;
import org.ayomide.dto.request.SignupRequest;
import org.ayomide.dto.request.VerifyRequest;
import org.ayomide.dto.response.AuthRespond;
import org.ayomide.exception.ApiException;
import org.ayomide.model.EmailVerificationToken;
import org.ayomide.model.Role;
import org.ayomide.model.SessionToken;
import org.ayomide.model.User;
import org.ayomide.repository.EmailVerificationTokenRepository;
import org.ayomide.repository.SessionTokenRepository;
import org.ayomide.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
public class AuthenticationService implements AuthenticationServiceInterface {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(UserRepository userRepository, EmailVerificationTokenRepository emailVerificationTokenRepository, SessionTokenRepository sessionTokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public AuthRespond signUp(SignupRequest signupRequest) {
        Optional<User> existingUserOpt = userRepository.findByEmail(signupRequest.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (existingUser.isVerified()) {
                throw new ApiException("EMAIL_IN_USE", HttpStatus.CONFLICT);
            } else {
                EmailVerificationToken newToken = new EmailVerificationToken();
                newToken.setToken(UUID.randomUUID().toString());
                newToken.setUser(existingUser);
                newToken.setExpireAt(LocalDateTime.now().plusHours(24));
                emailVerificationTokenRepository.save(newToken);

                return new AuthRespond("VERIFICATION_RESENT", newToken.getToken());
            }
        }

        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setVerified(false);
        user.setRole(Role.TALENT);
        userRepository.save(user);

        EmailVerificationToken emailVerificationToken = new EmailVerificationToken();
        emailVerificationToken.setToken(UUID.randomUUID().toString());
        emailVerificationToken.setUser(user);
        emailVerificationToken.setExpireAt(LocalDateTime.now().plusHours(24));
        emailVerificationTokenRepository.save(emailVerificationToken);

        return new AuthRespond("PENDING_VERIFICATION, VERIFY YOUR EMAIL", emailVerificationToken.getToken());
    }

    @Override
    public AuthRespond verifyEmail(VerifyRequest verifyRequest) {
        Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenRepository.findByToken(verifyRequest.getToken());

        if (tokenOpt.isEmpty()) {
            throw new ApiException("TOKEN_INVALID", HttpStatus.BAD_REQUEST);
        }

        EmailVerificationToken token = tokenOpt.get();

        if (token.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("TOKEN_EXPIRED", HttpStatus.BAD_REQUEST);
        }

        User user = token.getUser();

        if (user.isVerified()) {
            throw new ApiException("TOKEN_ALREADY_USED", HttpStatus.BAD_REQUEST);
        }

        user.setVerified(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(token);

        return new AuthRespond("VERIFIED", null);
    }


    @Override
    public AuthRespond login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ApiException("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ApiException("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isVerified()) {
            throw new ApiException("EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN);
        }

        SessionToken sessionToken = new SessionToken();
        sessionToken.setToken(UUID.randomUUID().toString());
        sessionToken.setUser(user);
        sessionToken.setCreatedAt(LocalDateTime.now());
        sessionToken.setExpireAt(LocalDateTime.now().plusHours(12));
        sessionTokenRepository.save(sessionToken);

        return new AuthRespond("LOGIN_SUCCESS", sessionToken.getToken());
    }


    @Override
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new ApiException("Invalid token format", HttpStatus.BAD_REQUEST);
        }
        String token = bearerToken.substring(7);

        SessionToken sessionToken = sessionTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid token", HttpStatus.UNAUTHORIZED));

        sessionTokenRepository.delete(sessionToken);

    }

}

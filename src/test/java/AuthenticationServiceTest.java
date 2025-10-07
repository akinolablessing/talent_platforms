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
import org.ayomide.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class AuthenticationServiceTest {



    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private SessionTokenRepository sessionTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private VerifyRequest verifyRequest;
    private User user;
    private EmailVerificationToken emailToken;
    private SessionToken sessionToken;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("odunayo@gmail.com");
        signupRequest.setPassword("odunayo123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("odunayo@gmail.com");
        loginRequest.setPassword("odunayo123");

        verifyRequest = new VerifyRequest();
        verifyRequest.setToken("7df9ec39-dce7-4f11-8a77-f763d0e58ef7");

        user = new User();
        user.setId(1L);
        user.setEmail("odunayo@gmail.com");
        user.setPassword("odunayo123");
        user.setVerified(false);
        user.setRole(Role.TALENT);

        emailToken = new EmailVerificationToken();
        emailToken.setToken("verification-token");
        emailToken.setUser(user);
        emailToken.setExpireAt(LocalDateTime.now().plusHours(1));

        sessionToken = new SessionToken();
        sessionToken.setToken("session-token");
        sessionToken.setUser(user);
        sessionToken.setCreatedAt(LocalDateTime.now());
        sessionToken.setExpireAt(LocalDateTime.now().plusHours(12));
    }


    @Test
    void signUp_NewUser_ShouldCreateUserAndReturnVerificationToken() {
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class))).thenReturn(emailToken);


        AuthRespond response = authenticationService.signUp(signupRequest);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("PENDING_VERIFICATION"));
        assertNotNull(response.getToken());
        System.out.println("Generated token: " + response.getToken());
        verify(userRepository).findByEmail(signupRequest.getEmail());
        verify(passwordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
    }

    @Test
    void signUp_ExistingVerifiedUser_ShouldThrowEmailInUseException() {
        user.setVerified(true);
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(user));

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.signUp(signupRequest));

        assertEquals("EMAIL_IN_USE", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(userRepository).findByEmail(signupRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signUp_ExistingUnverifiedUser_ShouldResendVerification() {
        user.setVerified(false);
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(user));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class))).thenReturn(emailToken);


        AuthRespond response = authenticationService.signUp(signupRequest);

        // Then
        assertNotNull(response);
        assertEquals("VERIFICATION_RESENT", response.getMessage());
        assertNotNull(response.getToken());

        verify(userRepository).findByEmail(signupRequest.getEmail());
        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void verifyEmail_ValidToken_ShouldVerifyUserAndDeleteToken() {
        when(emailVerificationTokenRepository.findByToken(verifyRequest.getToken()))
                .thenReturn(Optional.of(emailToken));
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthRespond response = authenticationService.verifyEmail(verifyRequest);

        assertNotNull(response);
        assertEquals("VERIFIED", response.getMessage());
        assertNull(response.getToken());

        verify(emailVerificationTokenRepository).findByToken(verifyRequest.getToken());
        verify(userRepository).save(user);
        verify(emailVerificationTokenRepository).delete(emailToken);
        assertTrue(user.isVerified());
    }

    @Test
    void verifyEmail_InvalidToken_ShouldThrowTokenInvalidException() {
        when(emailVerificationTokenRepository.findByToken(verifyRequest.getToken()))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.verifyEmail(verifyRequest));

        assertEquals("TOKEN_INVALID", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(emailVerificationTokenRepository).findByToken(verifyRequest.getToken());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmail_ExpiredToken_ShouldThrowTokenExpiredException() {
        emailToken.setExpireAt(LocalDateTime.now().minusHours(1));
        when(emailVerificationTokenRepository.findByToken(verifyRequest.getToken()))
                .thenReturn(Optional.of(emailToken));

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.verifyEmail(verifyRequest));

        assertEquals("TOKEN_EXPIRED", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(emailVerificationTokenRepository).findByToken(verifyRequest.getToken());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmail_AlreadyVerifiedUser_ShouldThrowTokenAlreadyUsedException() {
        user.setVerified(true);
        when(emailVerificationTokenRepository.findByToken(verifyRequest.getToken()))
                .thenReturn(Optional.of(emailToken));

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.verifyEmail(verifyRequest));

        assertEquals("TOKEN_ALREADY_USED", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(emailVerificationTokenRepository).findByToken(verifyRequest.getToken());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void login_ValidCredentialsAndVerifiedUser_ShouldReturnSessionToken() {
        user.setVerified(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(sessionTokenRepository.save(any(SessionToken.class))).thenReturn(sessionToken);

        AuthRespond response = authenticationService.login(loginRequest);

        assertNotNull(response);
        assertEquals("LOGIN_SUCCESS", response.getMessage());
        assertNotNull(response.getToken());
        System.out.print(response.getToken());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(sessionTokenRepository).save(any(SessionToken.class));
    }

    @Test
    void login_UserNotFound_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.login(loginRequest));

        assertEquals("INVALID_CREDENTIALS", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.login(loginRequest));

        assertEquals("INVALID_CREDENTIALS", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(sessionTokenRepository, never()).save(any(SessionToken.class));
    }

    @Test
    void login_UnverifiedUser_ShouldThrowEmailNotVerifiedException() {
        user.setVerified(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.login(loginRequest));

        assertEquals("EMAIL_NOT_VERIFIED", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(sessionTokenRepository, never()).save(any(SessionToken.class));
    }


    @Test
    void logout_ValidBearerToken_ShouldDeleteSessionToken() {
        String bearerToken = "Bearer session-token";
        when(sessionTokenRepository.findByToken("session-token")).thenReturn(Optional.of(sessionToken));

        authenticationService.logout(bearerToken);

        verify(sessionTokenRepository).findByToken("session-token");
        verify(sessionTokenRepository).delete(sessionToken);
    }

    @Test
    void logout_InvalidTokenFormat_ShouldThrowInvalidTokenFormatException() {
        String invalidToken = "session-token";

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.logout(invalidToken));

        assertEquals("Invalid token format", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(sessionTokenRepository, never()).findByToken(anyString());
    }

    @Test
    void logout_NullToken_ShouldThrowInvalidTokenFormatException() {
        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.logout(null));

        assertEquals("Invalid token format", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(sessionTokenRepository, never()).findByToken(anyString());
    }

    @Test
    void logout_TokenNotFound_ShouldThrowInvalidTokenException() {
        String bearerToken = "Bearer invalid-token";
        when(sessionTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.logout(bearerToken));

        assertEquals("Invalid token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(sessionTokenRepository).findByToken("invalid-token");
        verify(sessionTokenRepository, never()).delete(any(SessionToken.class));
    }


    @Test
    void signUp_NullEmail_ShouldHandleGracefully() {
        signupRequest.setEmail(null);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());


        assertDoesNotThrow(() -> authenticationService.signUp(signupRequest));
    }

    @Test
    void verifyEmail_TokenExpiresExactlyNow_ShouldThrowTokenExpiredException() {
        emailToken.setExpireAt(LocalDateTime.now());
        when(emailVerificationTokenRepository.findByToken(verifyRequest.getToken()))
                .thenReturn(Optional.of(emailToken));

        ApiException exception = assertThrows(ApiException.class,
                () -> authenticationService.verifyEmail(verifyRequest));

        assertEquals("TOKEN_EXPIRED", exception.getMessage());
    }

}


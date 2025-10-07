import org.ayomide.dto.request.SignupRequest;
import org.ayomide.dto.response.AuthRespond;
import org.ayomide.exception.ApiException;
import org.ayomide.model.EmailVerificationToken;
import org.ayomide.model.User;
import org.ayomide.repository.EmailVerificationTokenRepository;
import org.ayomide.repository.SessionTokenRepository;
import org.ayomide.repository.UserRepository;
import org.ayomide.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
        }
    @Test
    void testSignUp_NewUser_Success() {
        SignupRequest request = new SignupRequest("akinolablessing1890@gmail.com", "mhide123");

        when(userRepository.findByEmail("akinolablessing1890@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("mhide123")).thenReturn("encoded");

        AuthRespond response = authenticationService.signUp(request);

        assertEquals("PENDING_VERIFICATION, VERIFY YOUR EMAIL", response.getMessage());
        assertNotNull(response.getToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailVerificationTokenRepository, times(1)).save(any(EmailVerificationToken.class));
    }

    @Test
    void testSignUp_ExistingVerifiedUser_ThrowsEmailInUse() {
        User existingUser = new User();
        existingUser.setVerified(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        SignupRequest request = new SignupRequest("test@example.com", "password");

        ApiException ex = assertThrows(ApiException.class, () -> authenticationService.signUp(request));
        assertEquals("EMAIL_IN_USE", ex.getMessage());
    }

}


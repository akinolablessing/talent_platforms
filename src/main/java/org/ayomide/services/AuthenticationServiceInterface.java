package org.ayomide.services;

import org.ayomide.dto.request.LoginRequest;
import org.ayomide.dto.request.SignupRequest;
import org.ayomide.dto.request.VerifyRequest;
import org.ayomide.dto.response.AuthRespond;

public interface AuthenticationServiceInterface {

     AuthRespond signUp(SignupRequest signupRequest);
     AuthRespond verifyEmail(VerifyRequest verifyRequest);
     AuthRespond login(LoginRequest loginRequest);
     void logout(String bearerToken);
}

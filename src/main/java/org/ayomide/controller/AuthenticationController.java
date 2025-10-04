package org.ayomide.controller;

import org.ayomide.dto.request.LoginRequest;
import org.ayomide.dto.request.SignupRequest;
import org.ayomide.dto.request.VerifyRequest;
import org.ayomide.dto.response.AuthRespond;
import org.ayomide.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthRespond> signup(@RequestBody SignupRequest request) {
        AuthRespond response = authenticationService.signUp(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthRespond> verifyEmail(@RequestBody VerifyRequest verifyRequest){
        AuthRespond respond = authenticationService.verifyEmail(verifyRequest);
        return ResponseEntity.ok(respond);
    }
    @PostMapping("/login")
    public ResponseEntity<AuthRespond> login(@RequestBody LoginRequest loginRequest) {
        AuthRespond response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        authenticationService.logout(bearerToken);
        return ResponseEntity.noContent().build();
    }

}

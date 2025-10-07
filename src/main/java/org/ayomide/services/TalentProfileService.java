package org.ayomide.services;

import org.ayomide.dto.request.TalentProfileRequest;
import org.ayomide.dto.response.TalentProfileResponse;
import org.ayomide.exception.ApiException;
import org.ayomide.model.SessionToken;
import org.ayomide.model.TalentProfile;
import org.ayomide.model.User;
import org.ayomide.repository.SessionTokenRepository;
import org.ayomide.repository.TalentProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TalentProfileService {

    private final TalentProfileRepository repository;
    private final SessionTokenRepository sessionTokenRepository;

    public TalentProfileService(TalentProfileRepository repository, SessionTokenRepository sessionTokenRepository) {
        this.repository = repository;
        this.sessionTokenRepository = sessionTokenRepository;
    }

    public TalentProfileResponse createOrUpdateProfile(String bearerToken, TalentProfileRequest request) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new ApiException("Invalid token format", HttpStatus.BAD_REQUEST);
        }

        String token = bearerToken.substring(7);

        SessionToken sessionToken = sessionTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid or expired token", HttpStatus.UNAUTHORIZED));

        User user = sessionToken.getUser();
        if (user == null) {
            throw new ApiException("User not found", HttpStatus.UNAUTHORIZED);
        }

        TalentProfile profile = repository.findByUserId(user.getId()).orElse(null);

        if (profile == null) {
            profile = new TalentProfile();
            profile.setUser(user);
        }

        if (request.getTranscript() != null) {
            profile.setTranscript(request.getTranscript());
        }
        if (request.getStatementOfPurpose() != null) {
            profile.setStatementOfPurpose(request.getStatementOfPurpose());
        }

        TalentProfile saved = repository.save(profile);

        return buildResponse(saved, "Profile updated successfully");
    }

    public TalentProfileResponse getMyProfile(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new ApiException("Invalid token format", HttpStatus.BAD_REQUEST);
        }

        String token = bearerToken.substring(7);

        SessionToken sessionToken = sessionTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid or expired token", HttpStatus.UNAUTHORIZED));

        User user = sessionToken.getUser();
        if (user == null) {
            throw new ApiException("User not found", HttpStatus.UNAUTHORIZED);
        }

        TalentProfile profile = repository.findByUserId(user.getId()).orElse(null);

        if (profile == null) {
            return new TalentProfileResponse(
                    null, "", "", 0,
                    List.of("transcript", "statementOfPurpose"),
                    "Profile not found - please create one"
            );
        }

        return buildResponse(profile, "Profile retrieved successfully");
    }

    private TalentProfileResponse buildResponse(TalentProfile profile, String message) {
        int completeness = 0;
        List<String> missing = new ArrayList<>();

        String transcript = profile.getTranscript();
        if (transcript != null && !transcript.trim().isEmpty()) {
            completeness += 50;
        } else {
            missing.add("transcript");
        }

        String statementOfPurpose = profile.getStatementOfPurpose();
        if (statementOfPurpose != null && !statementOfPurpose.trim().isEmpty()) {
            completeness += 50;
        } else {
            missing.add("statementOfPurpose");
        }

        return new TalentProfileResponse(
                profile.getId(),
                transcript,
                statementOfPurpose,
                completeness,
                missing,
                message
        );
    }
}
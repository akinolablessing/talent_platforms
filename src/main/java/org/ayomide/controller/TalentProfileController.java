package org.ayomide.controller;

import jakarta.validation.Valid;
import org.ayomide.dto.request.TalentProfileRequest;
import org.ayomide.dto.response.TalentProfileResponse;
import org.ayomide.services.TalentProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import javax.validation.Valid;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class TalentProfileController {

    private final TalentProfileService talentProfileService;

    public TalentProfileController(TalentProfileService talentProfileService) {
        this.talentProfileService = talentProfileService;
    }

    /**
     * Create or update talent profile
     * POST /profile/post-profile
     */
    @PostMapping("/post-profile")
    public ResponseEntity<TalentProfileResponse> createOrUpdateProfile(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody TalentProfileRequest request) {

        try {
            TalentProfileResponse response = talentProfileService.createOrUpdateProfile(bearerToken, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error and return a debug response
            System.out.println("Error in createOrUpdateProfile: " + e.getMessage());
            e.printStackTrace();

            // Return a debug response to help identify the issue
            TalentProfileResponse debugResponse = new TalentProfileResponse(
                    null,
                    "DEBUG: " + request.getTranscript(),
                    "DEBUG: " + request.getStatementOfPurpose(),
                    0,
                    null,
                    "Error: " + e.getMessage()
            );
            return ResponseEntity.ok(debugResponse);
        }
    }

    /**
     * Get current user's talent profile
     * GET /profile
     */
    @GetMapping
    public ResponseEntity<TalentProfileResponse> getMyProfile(
            @RequestHeader("Authorization") String bearerToken) {

        TalentProfileResponse response = talentProfileService.getMyProfile(bearerToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Update specific fields of talent profile (PATCH)
     * PATCH /profile/post-profile
     */
    @PatchMapping("/patch-profile")
    public ResponseEntity<TalentProfileResponse> updateProfile(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody TalentProfileRequest request) {

        TalentProfileResponse response = talentProfileService.createOrUpdateProfile(bearerToken, request);
        return ResponseEntity.ok(response);
    }
}
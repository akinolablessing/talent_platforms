package org.ayomide.services;

import org.ayomide.dto.request.TalentProfileRequest;
import org.ayomide.dto.response.TalentProfileResponse;
import org.ayomide.model.TalentProfile;

public interface TalentProfileServiceInterface {

    TalentProfileResponse createOrUpdateProfile(String bearerToken, TalentProfileRequest request);
    TalentProfileResponse buildResponse(TalentProfile profile, String message);
}

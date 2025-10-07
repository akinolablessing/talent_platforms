package org.ayomide.dto.response;

import java.util.List;

public class TalentProfileResponse {
    private Long profileId;
    private String transcript;
    private String statementOfPurpose;
    private int completenessPercentage;
    private List<String> missingFields;
    private String message;

    public TalentProfileResponse(Long id, String transcript, String statementOfPurpose,
                                 int completeness, List<String> missing, String message) {
        this.profileId = id;
        this.transcript = transcript;
        this.statementOfPurpose = statementOfPurpose;
        this.completenessPercentage = completeness;
        this.missingFields = missing;
        this.message = message;
    }


    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getStatementOfPurpose() {
        return statementOfPurpose;
    }

    public void setStatementOfPurpose(String statementOfPurpose) {
        this.statementOfPurpose = statementOfPurpose;
    }

    public int getCompletenessPercentage() {
        return completenessPercentage;
    }

    public void setCompletenessPercentage(int completenessPercentage) {
        this.completenessPercentage = completenessPercentage;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}

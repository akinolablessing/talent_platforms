package org.ayomide.dto.request;

public class ProfileRequest {

    private String transcript;
    private String statementOfPurpose;

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
}

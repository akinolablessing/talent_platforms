package org.ayomide.dto.request;

public class SignupRequest {

    private String email;
    private String password;


    public SignupRequest() {

    }

    public SignupRequest(String mail, String password) {
        this.email = mail;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

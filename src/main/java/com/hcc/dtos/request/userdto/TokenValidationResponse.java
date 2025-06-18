package com.hcc.dtos.request.userdto;

public class TokenValidationResponse {
    private boolean isValid;
    private String message;

    public TokenValidationResponse(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
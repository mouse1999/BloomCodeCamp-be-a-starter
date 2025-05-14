package com.hcc.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String invalidCredentials) {
    }
}

package com.hcc.exceptions.rolexceptions;

import com.hcc.exceptions.userexceptions.UserException;

public class InvalidRoleException extends UserException {
    public InvalidRoleException(String message) {
        super(message);
    }

    public InvalidRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}

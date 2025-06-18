package com.hcc.exceptions.assignmentexceptions;

public class InvalidStatusChangeException extends AssignmentException {
    public InvalidStatusChangeException(String message) {
        super(message);
    }

    public InvalidStatusChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}

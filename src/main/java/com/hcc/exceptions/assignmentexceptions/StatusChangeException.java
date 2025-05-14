package com.hcc.exceptions.assignmentexceptions;

public class StatusChangeException extends AssignmentException{
    public StatusChangeException(String message) {
        super(message);
    }

    public StatusChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}

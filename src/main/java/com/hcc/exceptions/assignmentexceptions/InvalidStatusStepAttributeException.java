package com.hcc.exceptions.assignmentexceptions;

public class InvalidStatusStepAttributeException extends AssignmentException {
    public InvalidStatusStepAttributeException(String message) {
        super(message);
    }

    public InvalidStatusStepAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}

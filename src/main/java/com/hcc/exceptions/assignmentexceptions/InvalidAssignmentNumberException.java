package com.hcc.exceptions.assignmentexceptions;

public class InvalidAssignmentNumberException extends AssignmentException {
    public InvalidAssignmentNumberException(String message) {
        super(message);
    }

    public InvalidAssignmentNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}

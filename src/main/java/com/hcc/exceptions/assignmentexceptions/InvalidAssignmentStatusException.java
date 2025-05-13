package com.hcc.exceptions.assignmentexceptions;

public class InvalidAssignmentStatusException extends AssignmentException{
    public InvalidAssignmentStatusException(String message) {
        super(message);
    }

    public InvalidAssignmentStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}

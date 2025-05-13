package com.hcc.exceptions.assignmentexceptions;

public class AssignmentAlreadySubmittedException extends AssignmentException{
    public AssignmentAlreadySubmittedException(String message) {
        super(message);
    }

    public AssignmentAlreadySubmittedException(String message, Throwable cause) {
        super(message, cause);
    }
}

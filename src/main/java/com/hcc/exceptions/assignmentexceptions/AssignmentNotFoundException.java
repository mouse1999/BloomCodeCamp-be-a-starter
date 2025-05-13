package com.hcc.exceptions.assignmentexceptions;

public class AssignmentNotFoundException extends AssignmentException{
    public AssignmentNotFoundException(String message) {
        super(message);
    }

    public AssignmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

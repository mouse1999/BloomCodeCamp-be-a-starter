package com.hcc.exceptions.assignmentexceptions;

public class AssignmentException extends RuntimeException{

    public AssignmentException(String message) {
        super(message);
    }

    public AssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.hcc.exceptions.assignmentexceptions;

public class InvalidAssignmentIdException extends AssignmentException{


    public InvalidAssignmentIdException(String message) {
        super(message);
    }

    public InvalidAssignmentIdException(String message, Throwable cause) {
        super(message, cause);
    }
}

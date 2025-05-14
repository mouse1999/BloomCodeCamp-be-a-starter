package com.hcc.exceptions.assignmentexceptions;

public class InvalidBranchNameException extends AssignmentException {
    public InvalidBranchNameException(String message) {
        super(message);
    }

    public InvalidBranchNameException(String message, Throwable cause) {
        super(message, cause);
    }
}

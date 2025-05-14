package com.hcc.exceptions.assignmentexceptions;

public class InvalidGithubUrlException extends AssignmentException{
    public InvalidGithubUrlException(String message) {
        super(message);
    }

    public InvalidGithubUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}

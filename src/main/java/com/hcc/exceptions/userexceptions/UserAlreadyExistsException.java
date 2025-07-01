package com.hcc.exceptions.userexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends UserException {
    @Serial
    private static final long serialVersionUID = 8879738954353093256L;

    public UserAlreadyExistsException(String message) {
        super(message);
    }
    public UserAlreadyExistsException(Throwable cause, String message) {
        super(message, cause);
    }

}

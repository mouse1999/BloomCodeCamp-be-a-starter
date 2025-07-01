package com.hcc.exceptions.userexceptions;

import com.hcc.exceptions.userexceptions.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends UserException {
    @Serial
    private static final long serialVersionUID = -4415025899945373538L;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

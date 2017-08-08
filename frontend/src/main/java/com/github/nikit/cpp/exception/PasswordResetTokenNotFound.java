package com.github.nikit.cpp.exception;

public class PasswordResetTokenNotFound extends RuntimeException {
    private static final long serialVersionUID = 952486328852702273L;

    public PasswordResetTokenNotFound(String message) {
        super(message);
    }
}

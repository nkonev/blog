package com.github.nikit.cpp.exception;

public class DataNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -7006664788237375370L;

    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException() { }
}

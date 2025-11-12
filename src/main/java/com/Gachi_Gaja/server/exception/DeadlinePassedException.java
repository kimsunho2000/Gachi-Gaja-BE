package com.Gachi_Gaja.server.exception;

public class DeadlinePassedException extends RuntimeException {
    public DeadlinePassedException(String message) {
        super(message);
    }
}

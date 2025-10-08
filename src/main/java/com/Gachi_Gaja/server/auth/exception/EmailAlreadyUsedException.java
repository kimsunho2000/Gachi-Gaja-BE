package com.Gachi_Gaja.server.auth.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String msg) { super(msg); }
}

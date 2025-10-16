package com.Gachi_Gaja.server.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String msg) { super(msg); }
}

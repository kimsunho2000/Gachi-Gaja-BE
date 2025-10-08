package com.Gachi_Gaja.server.auth.exception;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String msg) { super(msg); }
}

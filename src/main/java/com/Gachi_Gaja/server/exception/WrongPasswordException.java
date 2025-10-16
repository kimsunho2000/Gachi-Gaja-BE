package com.Gachi_Gaja.server.exception;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String msg) { super(msg); }
}

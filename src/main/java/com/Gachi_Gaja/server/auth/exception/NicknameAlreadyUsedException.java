package com.Gachi_Gaja.server.auth.exception;

public class NicknameAlreadyUsedException extends RuntimeException {
    public NicknameAlreadyUsedException(String msg) { super(msg); }
}

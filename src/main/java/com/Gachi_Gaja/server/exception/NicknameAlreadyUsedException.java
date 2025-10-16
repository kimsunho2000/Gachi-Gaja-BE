package com.Gachi_Gaja.server.exception;

public class NicknameAlreadyUsedException extends RuntimeException {
    public NicknameAlreadyUsedException(String msg) { super(msg); }
}

package com.Gachi_Gaja.server.exception;

public class GroupFullException extends RuntimeException {
    public GroupFullException(String message) {
        super(message);
    }
}

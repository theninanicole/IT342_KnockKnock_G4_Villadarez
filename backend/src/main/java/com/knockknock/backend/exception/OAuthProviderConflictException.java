package com.knockknock.backend.exception;

public class OAuthProviderConflictException extends RuntimeException {

    public OAuthProviderConflictException(String message) {
        super(message);
    }
}

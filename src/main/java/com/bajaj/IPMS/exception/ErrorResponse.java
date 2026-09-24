package com.bajaj.IPMS.exception;

import java.time.OffsetDateTime;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public ErrorResponse(int status, String error, String message, String path){
        this(OffsetDateTime.now(), status, error, message, path);
    }
}

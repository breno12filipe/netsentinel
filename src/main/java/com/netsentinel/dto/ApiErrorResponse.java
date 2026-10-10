package com.netsentinel.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        List<String> details,
        LocalDateTime timestamp
) {
    public ApiErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(status, error, message, path, details, LocalDateTime.now());
    }
}

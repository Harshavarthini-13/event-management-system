package com.eventmgmt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper.
 * Every API endpoint returns this structure for consistency.
 *
 * Success example:
 * {
 *   "success": true,
 *   "message": "Event created successfully",
 *   "data": { ... },
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 *
 * Error example:
 * {
 *   "success": false,
 *   "message": "Event not found",
 *   "data": null,
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Static factory methods for convenience
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
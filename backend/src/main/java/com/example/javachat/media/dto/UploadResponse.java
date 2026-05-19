package com.example.javachat.media.dto;

public record UploadResponse(
        String url,
        String contentType,
        long size
) {
}

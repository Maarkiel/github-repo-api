package com.example.githubrepoapi.dto;

public record ErrorResponse(
    int status,
    String message
) {
}


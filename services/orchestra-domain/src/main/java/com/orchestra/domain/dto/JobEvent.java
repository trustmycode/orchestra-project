package com.orchestra.domain.dto;

import java.time.LocalDateTime;

public record JobEvent(
    String stage,       // "ANALYSIS", "RESOLVER", "GENERATION"
    String description,
    Object data,        // Intermediate JSON payload
    LocalDateTime timestamp
) {
}


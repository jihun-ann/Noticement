package com.example.noticement.harness;

import java.util.Map;

public record GuardDecision(
        GuardStatus status,
        String code,
        String message,
        Map<String, Object> metadata
) {}

package com.example.noticement.harness.crawl;

import com.example.noticement.harness.GuardDecision;

public record ValidatedResponse(String body, GuardDecision decision) {}

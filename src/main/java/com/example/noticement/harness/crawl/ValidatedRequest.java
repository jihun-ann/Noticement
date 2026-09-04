package com.example.noticement.harness.crawl;

import com.example.noticement.harness.GuardDecision;

import java.net.URI;

public record ValidatedRequest(URI uri, GuardDecision decision) {}

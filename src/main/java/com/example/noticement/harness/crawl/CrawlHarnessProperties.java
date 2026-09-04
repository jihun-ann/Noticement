package com.example.noticement.harness.crawl;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "harness.crawl")
public record CrawlHarnessProperties(
        boolean blockPrivateNetwork,
        int maxRedirects,
        long connectTimeoutMs,
        long readTimeoutMs,
        long maxBodyBytes,
        List<String> allowedSchemes,
        List<String> allowedContentTypes
) {}

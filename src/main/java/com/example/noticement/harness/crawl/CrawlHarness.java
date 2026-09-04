package com.example.noticement.harness.crawl;

import java.net.URI;
import java.net.http.HttpResponse;

public interface CrawlHarness {
    ValidatedRequest validateBeforeRequest(URI uri);
    ValidatedResponse validateAfterResponse(ValidatedRequest request, HttpResponse<?> response);
}

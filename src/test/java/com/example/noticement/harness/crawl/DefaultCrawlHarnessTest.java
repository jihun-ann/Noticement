package com.example.noticement.harness.crawl;

import com.example.noticement.harness.GuardStatus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultCrawlHarnessTest {

    private final DefaultCrawlHarness harness = new DefaultCrawlHarness(new CrawlHarnessProperties(
            true, 3, 3000, 10000, 5_242_880,
            List.of("https", "http"),
            List.of("text/html", "application/json")
    ));

    @Test
    void blocksLoopbackHost() {
        var result = harness.validateBeforeRequest(URI.create("http://127.0.0.1/admin"));
        assertEquals(GuardStatus.BLOCK, result.decision().status());
    }

    @Test
    void blocksCloudMetadataEndpoint() {
        var result = harness.validateBeforeRequest(URI.create("http://169.254.169.254/latest/meta-data"));
        assertEquals(GuardStatus.BLOCK, result.decision().status());
    }

    @Test
    void blocksDisallowedScheme() {
        var result = harness.validateBeforeRequest(URI.create("file:///etc/passwd"));
        assertEquals(GuardStatus.BLOCK, result.decision().status());
    }

    @Test
    void allowsPublicHttpsHost() {
        var result = harness.validateBeforeRequest(URI.create("https://example.com/release-notes"));
        assertEquals(GuardStatus.ALLOW, result.decision().status());
    }
}

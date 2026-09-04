package com.example.noticement.harness.crawl;

import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
@EnableConfigurationProperties(CrawlHarnessProperties.class)
public class DefaultCrawlHarness implements CrawlHarness {

    private final CrawlHarnessProperties properties;

    public DefaultCrawlHarness(CrawlHarnessProperties properties) {
        this.properties = properties;
    }

    @Override
    public ValidatedRequest validateBeforeRequest(URI uri) {
        if (uri.getScheme() == null || !properties.allowedSchemes().contains(uri.getScheme())) {
            return new ValidatedRequest(uri, block("DISALLOWED_SCHEME", "scheme not in allowlist: " + uri.getScheme()));
        }

        if (properties.blockPrivateNetwork() && isPrivateOrInternalHost(uri.getHost())) {
            return new ValidatedRequest(uri, block("PRIVATE_NETWORK_TARGET", "target resolves to a private/internal address"));
        }

        return new ValidatedRequest(uri, allow());
    }

    @Override
    public ValidatedResponse validateAfterResponse(ValidatedRequest request, HttpResponse<?> response) {
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        boolean mimeOk = properties.allowedContentTypes().stream().anyMatch(contentType::contains);
        if (!mimeOk) {
            return new ValidatedResponse(null, block("REJECTED_MIME", "content-type not allowed: " + contentType));
        }

        long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
        if (contentLength > properties.maxBodyBytes()) {
            return new ValidatedResponse(null, block("REJECTED_TOO_LARGE", "content-length exceeds max: " + contentLength));
        }

        return new ValidatedResponse(null, allow());
    }

    private boolean isPrivateOrInternalHost(String host) {
        if (host == null) {
            return true;
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()
                    || address.isAnyLocalAddress()
                    || address.isMulticastAddress();
        } catch (UnknownHostException e) {
            return true;
        }
    }

    private GuardDecision allow() {
        return new GuardDecision(GuardStatus.ALLOW, "OK", null, Map.of());
    }

    private GuardDecision block(String code, String message) {
        return new GuardDecision(GuardStatus.BLOCK, code, message, Map.of());
    }
}

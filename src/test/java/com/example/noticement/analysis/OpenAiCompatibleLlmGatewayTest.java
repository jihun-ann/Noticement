package com.example.noticement.analysis;

import com.example.noticement.analysis.LlmGatewayProperties.ProviderConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAiCompatibleLlmGatewayTest {

    private HttpServer server;
    private OpenAiCompatibleLlmGateway gateway;

    @BeforeEach
    void startFakeProvider() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            String responseBody = """
                    {"choices":[{"message":{"content":"{\\"value\\":\\"hello\\"}"}}]}
                    """;
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        ProviderConfig config = new ProviderConfig("test", "http://localhost:" + server.getAddress().getPort(), "test-key", "test-model");
        LlmGatewayProperties properties = new LlmGatewayProperties(config, config, 5000);
        gateway = new OpenAiCompatibleLlmGateway(properties, new ObjectMapper());
    }

    @AfterEach
    void stopFakeProvider() {
        server.stop(0);
    }

    @Test
    void parsesChatCompletionContentIntoResponseType() {
        LlmRequest request = new LlmRequest(LlmPurpose.ANALYSIS, "system", "policy", "document");

        TestPayload result = gateway.generate(request, TestPayload.class);

        assertEquals("hello", result.value());
    }

    private record TestPayload(String value) {}
}

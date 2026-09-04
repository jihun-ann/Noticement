package com.example.noticement.notion;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.DocumentCategory;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;
import com.example.noticement.persistence.entity.NotionPublishRecord;
import com.example.noticement.persistence.repository.NotionPublishRecordRepository;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotionApiPublisherTest {

    private HttpServer server;

    private final TechDocument document = new TechDocument(
            UUID.randomUUID(), "src", "https://example.com", "title", "vendor",
            DocumentCategory.JAVA, Instant.now(), Instant.now(), "content", "hash"
    );

    private final DocumentAnalysis analysis = new DocumentAnalysis(
            document.id(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
            50, 0.9, List.of()
    );

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void publishesNewDocumentAndPersistsRecord() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/pages", exchange -> {
            String body = """
                    {"id":"page-123","url":"https://notion.so/page-123"}
                    """;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        NotionPublishRecordRepository repository = mock(NotionPublishRecordRepository.class);
        when(repository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotionApiPublisher publisher = new NotionApiPublisher(
                new NotionProperties("http://localhost:" + server.getAddress().getPort(), "2022-06-28", "key", "db-1", 5000),
                new NotionPageMapper(),
                new NotionTemplateRenderer(),
                repository
        );

        PublishedPage page = publisher.publishDocument(document, analysis);

        assertEquals("page-123", page.pageId());
        assertEquals("https://notion.so/page-123", page.pageUrl());
        verify(repository).save(any(NotionPublishRecord.class));
    }

    @Test
    void skipsRepublishWhenIdempotencyKeyAlreadyPublished() {
        NotionPublishRecord existing = new NotionPublishRecord(UUID.randomUUID(), document.id(), "DOCUMENT", "notion:" + document.id() + ":v1");
        existing.markPublished("existing-page", "https://notion.so/existing-page");

        NotionPublishRecordRepository repository = mock(NotionPublishRecordRepository.class);
        when(repository.findByIdempotencyKey(any())).thenReturn(Optional.of(existing));

        NotionApiPublisher publisher = new NotionApiPublisher(
                new NotionProperties("http://localhost:1", "2022-06-28", "key", "db-1", 5000),
                new NotionPageMapper(),
                new NotionTemplateRenderer(),
                repository
        );

        PublishedPage page = publisher.publishDocument(document, analysis);

        assertEquals("existing-page", page.pageId());
        assertEquals("https://notion.so/existing-page", page.pageUrl());
        verify(repository, never()).save(any());
    }
}

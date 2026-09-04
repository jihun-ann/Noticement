package com.example.noticement.domain.document;

public record Evidence(
        String claim,
        String sourceUrl,
        String excerptHash
) {}

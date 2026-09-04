package com.example.noticement.analysis;

public interface LlmGateway {
    <T> T generate(LlmRequest request, Class<T> responseType);
}

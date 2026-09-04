package com.example.noticement.application;

public class PipelineRejectedException extends RuntimeException {
    public PipelineRejectedException(String code) {
        super(code);
    }
}

package com.example.noticement.harness.ai;

import com.example.noticement.harness.GuardDecision;

public interface PromptInjectionGuard {
    GuardDecision inspect(String document);
}

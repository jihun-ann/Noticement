package com.example.noticement.harness.ai;

import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// ponytail: naive keyword/phrase match, misses paraphrased or non-English injection attempts;
// upgrade to an LLM-based classifier pass if false negatives show up in practice.
@Component
@EnableConfigurationProperties(PromptInjectionGuardProperties.class)
public class DefaultPromptInjectionGuard implements PromptInjectionGuard {

    private final List<Pattern> suspiciousPatterns;

    public DefaultPromptInjectionGuard(PromptInjectionGuardProperties properties) {
        this.suspiciousPatterns = properties.suspiciousPhrases().stream()
                .map(phrase -> Pattern.compile(Pattern.quote(phrase), Pattern.CASE_INSENSITIVE))
                .toList();
    }

    @Override
    public GuardDecision inspect(String document) {
        if (document == null || document.isBlank()) {
            return allow();
        }

        for (Pattern pattern : suspiciousPatterns) {
            if (pattern.matcher(document).find()) {
                return new GuardDecision(
                        GuardStatus.REVIEW,
                        "SUSPICIOUS_INSTRUCTION_PATTERN",
                        "document contains a suspicious instruction-like phrase: " + pattern.pattern(),
                        Map.of()
                );
            }
        }

        return allow();
    }

    private GuardDecision allow() {
        return new GuardDecision(GuardStatus.ALLOW, "OK", null, Map.of());
    }
}

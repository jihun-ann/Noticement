package com.example.noticement.collector;

import java.util.List;

public interface SourceCollector {
    boolean supports(SourceConfig config);
    List<CollectedDocument> collect(SourceConfig config);
}

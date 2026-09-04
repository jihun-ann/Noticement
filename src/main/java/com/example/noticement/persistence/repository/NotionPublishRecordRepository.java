package com.example.noticement.persistence.repository;

import com.example.noticement.persistence.entity.NotionPublishRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotionPublishRecordRepository extends JpaRepository<NotionPublishRecord, UUID> {
    Optional<NotionPublishRecord> findByIdempotencyKey(String idempotencyKey);
}

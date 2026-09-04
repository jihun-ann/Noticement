package com.example.noticement.persistence.repository;

import com.example.noticement.persistence.entity.MailDeliveryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MailDeliveryRecordRepository extends JpaRepository<MailDeliveryRecord, UUID> {
    Optional<MailDeliveryRecord> findByIdempotencyKey(String idempotencyKey);
}

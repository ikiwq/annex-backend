package com.annex.backend.repositories;

import com.annex.backend.models.Notification;
import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query(value = "select n from Notification n WHERE n.recipient = :user AND n.createdAt <= :before ORDER BY n.createdAt DESC")
    List<Notification> getUserNotificationCreatedBefore(User user, Instant before, Pageable pageable);
}

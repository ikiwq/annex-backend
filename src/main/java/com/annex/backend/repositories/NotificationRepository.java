package com.annex.backend.repositories;

import com.annex.backend.models.Notification;
import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query(value = "SELECT n FROM Notification n WHERE n.recipient = :user ORDER BY n.notificationId DESC")
    List<Notification> getAllFromUserPag(User user, Pageable pageable);
    @Query(value = "SELECT n FROM Notification n WHERE n.recipient = :user AND n.id <= :cursor ORDER BY n.notificationId DESC")
    List<Notification> getAllFromUser(User user, Long cursor, Pageable pageable);
}

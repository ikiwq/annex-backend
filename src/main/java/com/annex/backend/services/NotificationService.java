package com.annex.backend.services;

import com.annex.backend.dto.NotificationDto;
import com.annex.backend.models.Notification;
import com.annex.backend.models.User;
import com.annex.backend.repositories.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotificationService {

    @Autowired
    NotificationRepository notificationRepository;
    UserService userService;
    PropertiesService propertiesService;

    private NotificationDto notificationToNotificationDto(Notification notification){
        NotificationDto notificationDto = new NotificationDto();

        notificationDto.setText(notification.getText());
        notificationDto.setImageUrl(propertiesService.backendAddress + "api/images/" + notification.getImageUrl());
        notificationDto.setToUrl(notification.getToUrl());
        notificationDto.setSeen(notification.isSeen());

        return notificationDto;
    }

    public Notification createNotification(User recipient, String text, String imgUrl, String redirect){
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setText(text);
        notification.setCreatedAt(Instant.now());
        notification.setImageUrl(imgUrl);
        notification.setToUrl(redirect);
        notification.setSeen(false);

        return notificationRepository.save(notification);
    }

    public ResponseEntity<List<NotificationDto>> getCurrentUserNotification(int page, Instant requestedAt){
        Pageable pageable = PageRequest.of(page, 10);
        List<NotificationDto> notificationlist = notificationRepository.getUserNotificationCreatedBefore(userService.getCurrentUser(), requestedAt, pageable).stream().map(this::notificationToNotificationDto).collect(Collectors.toList());
        return new ResponseEntity<>(notificationlist, HttpStatus.OK);
    }

    public ResponseEntity<String> seeNotification(long id){
        User currentUser = userService.getCurrentUser();
        Notification toSee = notificationRepository.getReferenceById(id);

        if(currentUser != toSee.getRecipient()){
            return new ResponseEntity<>("No permission!", HttpStatus.BAD_REQUEST);
        }

        if(toSee.isSeen()){
            return new ResponseEntity<>("Notification alredy seen!", HttpStatus.BAD_REQUEST);
        }

        toSee.setSeen(true);

        notificationRepository.save(toSee);

        return new ResponseEntity<>("Seen!", HttpStatus.OK);
    }
}

package com.annex.backend.services;

import com.annex.backend.dto.CursorNotificationsResponse;
import com.annex.backend.dto.NotificationDto;
import com.annex.backend.models.Notification;
import com.annex.backend.models.User;
import com.annex.backend.repositories.NotificationRepository;
import lombok.AllArgsConstructor;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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

        PrettyTime prettyTime = new PrettyTime();
        notificationDto.setCreatedAt(prettyTime.format(Date.from(notification.getCreatedAt())));

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

    public CursorNotificationsResponse getCurrentUserNotification(Long cursor, int pageSize){
        if(userService.getCurrentUser() == null){
            throw new IllegalStateException("User is not logged in");
        }
        CursorNotificationsResponse cursorNotificationsResponse = new CursorNotificationsResponse();
        if(cursor == -1){
            Pageable pag = PageRequest.of(0, 1);
            List<Notification> notificationCursors = notificationRepository.getAllFromUserPag(userService.getCurrentUser(), pag);
            if(notificationCursors.size() == 0) {
                cursorNotificationsResponse.setCursor(1L);
                cursorNotificationsResponse.setNotifications(new ArrayList<>());
                return cursorNotificationsResponse;
            }
            cursor = notificationCursors.get(0).getNotificationId();
        }
        Pageable pageable = PageRequest.of(0, pageSize);

        List<Notification> notifications = notificationRepository.getAllFromUser(userService.getCurrentUser(), cursor, pageable);
        List<NotificationDto> notificationDtos= notifications.stream().map(this::notificationToNotificationDto).collect(Collectors.toList());

        cursorNotificationsResponse.setNotifications(notificationDtos);

        if (notifications.size() == 0) {
            cursorNotificationsResponse.setCursor(1L);
        }else{
            cursorNotificationsResponse.setCursor(notifications.get(notifications.size()-1).getNotificationId());
        }

        return cursorNotificationsResponse;
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

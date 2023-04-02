package com.annex.backend.controllers;

import com.annex.backend.dto.CursorNotificationsResponse;
import com.annex.backend.dto.NotificationDto;
import com.annex.backend.services.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api/notification")
@AllArgsConstructor
public class NotificationController {
    @Autowired
    NotificationService notificationService;

    @GetMapping("/")
    public ResponseEntity<CursorNotificationsResponse> getUserNotification(@RequestParam Long cursor, int pageSize){
        return new ResponseEntity<>(notificationService.getCurrentUserNotification(cursor, pageSize), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> seeNotification(@PathVariable long id){
        return notificationService.seeNotification(id);
    }
}

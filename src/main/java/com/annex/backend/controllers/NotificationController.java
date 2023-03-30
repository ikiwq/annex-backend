package com.annex.backend.controllers;

import com.annex.backend.dto.NotificationDto;
import com.annex.backend.services.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<NotificationDto>> getUserNotification(@RequestParam int page, @RequestParam Instant openedAt){
        return notificationService.getCurrentUserNotification(page, openedAt);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> seeNotification(@PathVariable long id){
        return notificationService.seeNotification(id);
    }
}

package com.annex.backend.controllers;

import com.annex.backend.services.FollowService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/follow")
@AllArgsConstructor
public class FollowController {
    @Autowired
    FollowService followService;

    @GetMapping("/{userToFollow}")
    public ResponseEntity<String> followUser(@PathVariable String userToFollow){
        return followService.followUser(userToFollow);
    }
}

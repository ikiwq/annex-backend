package com.annex.backend.controllers;

import com.annex.backend.services.LikeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/like")
@AllArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/post/{id}")
    public ResponseEntity<String> likePost(@PathVariable Long id){
        return likeService.likePost(id);
    }
}

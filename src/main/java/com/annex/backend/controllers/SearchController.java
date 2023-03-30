package com.annex.backend.controllers;

import com.annex.backend.dto.PostResponse;
import com.annex.backend.dto.UserResponse;
import com.annex.backend.services.PostService;
import com.annex.backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api/search")
@AllArgsConstructor
public class SearchController {

    @Autowired
    PostService postService;

    @Autowired
    UserService userService;

    @GetMapping("/post/{postText}")
    public ResponseEntity<List<PostResponse>> searchPosts(@RequestParam int page, @RequestParam Instant startDate, @PathVariable String postText){
        System.out.println("text2: " + postText);
        return postService.searchPosts(postText, page, startDate);
    }

    @GetMapping("/user/{nickname}")
    public ResponseEntity<List<UserResponse>> searchUser(@PathVariable String nickname, @RequestParam Instant startDate){
        return userService.searchUsers(nickname, startDate);
    }

}

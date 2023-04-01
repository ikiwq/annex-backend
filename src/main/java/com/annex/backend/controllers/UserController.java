package com.annex.backend.controllers;

import com.annex.backend.dto.CursorPostsResponse;
import com.annex.backend.dto.EditRequest;
import com.annex.backend.dto.PostResponse;
import com.annex.backend.dto.UserResponse;
import com.annex.backend.models.User;
import com.annex.backend.services.PostService;
import com.annex.backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @GetMapping("/")
    public ResponseEntity<List<UserResponse>> getSuggested(){
        return userService.getSuggested();
    }

    @GetMapping("/{username}/exists")
    public ResponseEntity<Boolean> doesExist(@PathVariable String username){
        return new ResponseEntity<Boolean>(userService.doesUserExistByUsername(username), HttpStatus.OK);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByNickname(@PathVariable String username){
        return userService.getUserByUsername(username);
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<String> followUser(@PathVariable String username){
        return userService.followUser(username);
    }

    @PostMapping("/{username}/edit")
    public ResponseEntity<UserResponse> editUser(@PathVariable String username, @RequestPart(required = false) MultipartFile picture,
                                                 @RequestPart(required = false) MultipartFile background, @RequestPart String jsonString){
        return userService.editProfile(username, picture, background, jsonString);
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity<List<PostResponse>> getPostsFromUser(@Valid @PathVariable String username, @RequestParam Long cursor, @RequestParam int pageSize){
        return new ResponseEntity<>(postService.getPostsFromUser(username, cursor, pageSize), HttpStatus.OK);
    }

    @GetMapping("/{username}/liked")
    public ResponseEntity<CursorPostsResponse> getLikedFromUser(@Valid @PathVariable String username, @RequestParam Long cursor, @RequestParam int pageSize){
        return new ResponseEntity<>(postService.getLikedFromUser(username, cursor, pageSize), HttpStatus.OK);
    }

    @GetMapping("/{username}/saved")
    public ResponseEntity<CursorPostsResponse> getSavedFromUser(@Valid @PathVariable String username, @RequestParam Long cursor, @RequestParam int pageSize){
        return new ResponseEntity<>(postService.getSavedFromUser(username, cursor, pageSize), HttpStatus.OK);
    }
}

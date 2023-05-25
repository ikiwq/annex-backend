package com.annex.backend.controllers;

import com.annex.backend.dto.PostRequest;
import com.annex.backend.dto.PostResponse;
import com.annex.backend.services.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api/post")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/")
    public ResponseEntity<PostResponse> createpost(@RequestPart(value = "images", required = false)MultipartFile[] images, @RequestPart String jsonString){
        return new ResponseEntity<>(postService.save(jsonString, images, -1L), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id){
        return new ResponseEntity<>(postService.getPost(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<PostResponse> reply(@PathVariable Long id, @RequestPart(value = "images", required = false)MultipartFile[] images, @RequestPart String jsonString){
        return new ResponseEntity<>(postService.save(jsonString, images, id), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/reply")
    public ResponseEntity<List<PostResponse>> getReplies(@PathVariable Long id, @RequestParam Long cursor, @RequestParam int pageSize){
        return new ResponseEntity<>(postService.getReplies(id, cursor, pageSize), HttpStatus.OK);
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<String> deletePost(@PathVariable Long id){
        return postService.deletePost(id);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostResponse>> getAllPosts(@RequestParam Long cursor, @RequestParam int page_size){
        return new ResponseEntity<>(postService.getAllPosts(cursor, page_size), HttpStatus.OK);
    }

}

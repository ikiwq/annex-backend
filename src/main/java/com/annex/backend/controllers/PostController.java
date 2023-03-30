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
    public ResponseEntity createpost(@RequestPart(value = "images", required = false)MultipartFile[] images, @RequestPart String jsonString){
        return postService.save(jsonString, images);
    }

    @GetMapping("/page/{page}")
    public ResponseEntity<List<PostResponse>> getAllPosts(@PathVariable int page, @RequestParam Instant startingDate, @RequestParam(required = false) String tag){
        if(tag != null){
            return new ResponseEntity<List<PostResponse>>(postService.getPostsWithTag(tag, page, startingDate), HttpStatus.OK);
        }
        return new ResponseEntity<List<PostResponse>>(postService.getAllPosts(page, startingDate), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id){
        return new ResponseEntity<PostResponse>(postService.getPost(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<PostResponse> reply(@PathVariable Long id, @RequestBody PostRequest replyRequest){
        return new ResponseEntity<PostResponse>(postService.reply(replyRequest, id), HttpStatus.OK);
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<PostResponse>> getReplies(@PathVariable Long id, @RequestParam int page, @RequestParam Instant startingDate){
        return postService.getReplies(id, page, startingDate);
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<String> deletePost(@PathVariable Long id){
        return postService.deletePost(id);
    }
}

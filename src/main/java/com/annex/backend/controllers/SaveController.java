package com.annex.backend.controllers;

import com.annex.backend.dto.PostResponse;
import com.annex.backend.services.PostService;
import com.annex.backend.services.SaveService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@RestController
@RequestMapping("api/save")
public class SaveController {

    @Autowired
    private SaveService saveService;
    @Autowired
    private PostService postService;

    @PostMapping("/{id}")
    private ResponseEntity<String> savePost(@PathVariable Long id){
        return saveService.savePost(id);
    }

}

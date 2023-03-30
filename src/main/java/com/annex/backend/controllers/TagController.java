package com.annex.backend.controllers;

import com.annex.backend.dto.TagDto;
import com.annex.backend.models.Tag;
import com.annex.backend.services.TagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tags")
@AllArgsConstructor
@Slf4j
public class TagController {
    private final TagService tagService;

    @GetMapping("/")
    public ResponseEntity<List<TagDto>> getAllTags(){
        return new ResponseEntity<>(tagService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<TagDto>> getPopularTags(){
        return new ResponseEntity<>(tagService.getPopular(), HttpStatus.OK);
    }
}

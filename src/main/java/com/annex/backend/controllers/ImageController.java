package com.annex.backend.controllers;

import com.annex.backend.services.ImageService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@AllArgsConstructor
@NoArgsConstructor
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping("/{path}")
    public ResponseEntity<?> getImage(@PathVariable String path){
        return imageService.getImage(path);
    }

    /*@PostMapping("/")
    public void uploadImage(@RequestPart MultipartFile image){
        imageService.uploadImageWithoutUser(image);
    }*/
}

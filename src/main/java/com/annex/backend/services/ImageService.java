package com.annex.backend.services;

import com.annex.backend.models.Image;
import com.annex.backend.models.User;
import com.annex.backend.repositories.ImageRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageUtil imageUtil;

    @Transactional
    public Image uploadImage(MultipartFile image, User uploader) {
        Image newImage = new Image();
        if (!image.getContentType().split("/")[0].equals("image")) {
            throw new RuntimeException("File is not an image");
        }
        try {
            //the path to the image ex: https://localhost/api/image/path_to_image
            //will be a new UUID. Since the possibility of 2 UUIDs being the same is
            //ruffly 0.00000000006%. I guess that is safe without checking for duplicates C:
            newImage.setPath(UUID.randomUUID().toString());
            newImage.setData(imageUtil.compressImage(image.getBytes()));
            newImage.setType(image.getContentType());
            newImage.setUser(uploader);
            newImage.setUplaodedAt(Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return imageRepository.save(newImage);
    }

    @Transactional
    public Boolean deleteImageByUrl(String url){
        imageRepository.deleteByPath(url);
        return true;
    }

    @Transactional
    public ResponseEntity<byte[]> getImage(String path){
        Image image = imageRepository.findByPath(path).orElseThrow(()-> new RuntimeException("Image not found"));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf(image.getType())).body(imageUtil.decompressImage(image.getData()));
    }

}

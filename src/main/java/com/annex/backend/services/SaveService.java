package com.annex.backend.services;

import com.annex.backend.models.Post;
import com.annex.backend.models.Save;
import com.annex.backend.repositories.PostRepository;
import com.annex.backend.repositories.SaveRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class SaveService {

    private PostRepository postRepository;
    private SaveRepository saveRepository;
    private UserService userService;

    @Transactional
    public ResponseEntity<String> savePost(Long id){
        Post postToSave = postRepository.findById(id).orElseThrow(() -> new IllegalStateException("Post not found"));
        Optional<Save> possibleSave =  saveRepository.findByUserAndPost(userService.getCurrentUser(), postToSave);

        if(possibleSave.isPresent()){
            saveRepository.removeBySaveId(possibleSave.get().getSaveId());

            return new ResponseEntity<String>("Post like deleted", HttpStatus.OK);
        }else{
            Save newSave = new Save();
            newSave.setPost(postToSave);
            newSave.setUser(userService.getCurrentUser());
            newSave.setCreatedAt(Instant.now());
            saveRepository.save(newSave);
        }

        return new ResponseEntity<String>("Post saved", HttpStatus.OK);
    }
}

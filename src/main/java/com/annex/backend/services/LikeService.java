package com.annex.backend.services;

import com.annex.backend.models.LikeVote;
import com.annex.backend.models.Post;
import com.annex.backend.repositories.LikeRepository;
import com.annex.backend.repositories.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public ResponseEntity<String> likePost(Long id){
        Post postToLike = postRepository.findById(id).orElseThrow(() -> new IllegalStateException("Post not found"));
        Optional<LikeVote> possibleLike =  likeRepository.findByUserAndPost(userService.getCurrentUser(), postToLike);

        if(possibleLike.isPresent()){
            likeRepository.removeByLikeId(possibleLike.get().getLikeId());

            return new ResponseEntity<String>("Post like deleted", HttpStatus.OK);
        }else{
            LikeVote likeVote = new LikeVote();
            likeVote.setPost(postToLike);
            likeVote.setUser(userService.getCurrentUser());
            likeVote.setCreatedAt(Instant.now());
            likeRepository.save(likeVote);

            notificationService.createNotification(postToLike.getUser(), userService.getCurrentUser().getUsername() + " liked your post!",
                    userService.getCurrentUser().getProfilePicture().getPath(), "/post/" + postToLike.getPostId());
        }

        return new ResponseEntity<String>("Post liked", HttpStatus.OK);
    }
}

package com.annex.backend.services;

import com.annex.backend.models.Follow;
import com.annex.backend.models.User;
import com.annex.backend.repositories.FollowRepository;
import com.annex.backend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
public class FollowService {

    @Autowired
    FollowRepository followRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    NotificationService notificationService;

    @Transactional
    public ResponseEntity<String> followUser(String username){

        User following = userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found!"));
        User follower = userService.getCurrentUser();

        if(follower == following){
            return new ResponseEntity<String>("Cannot follow your self!", HttpStatus.OK);
        }

        Follow follow = followRepository.findFollow(follower, following).orElse(null);

        if(follow != null){
            followRepository.deleteById(follow.getFollowId());
            return new ResponseEntity<String>("Follow removed", HttpStatus.OK);
        }

        Follow newFollow = new Follow();

        newFollow.setFollowing(following);
        newFollow.setFollower(follower);
        newFollow.setFollowedAt(Instant.now());

        followRepository.save(newFollow);

        notificationService.createNotification(following, follower.getUsername() + " followed you!",
                userService.getCurrentUser().getProfilePicture().getPath(), "/profile/" + follower.getUsername());

        return new ResponseEntity<String>("User followed", HttpStatus.OK);
    }
}

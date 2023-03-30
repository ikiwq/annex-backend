package com.annex.backend.services;

import com.annex.backend.dto.EditRequest;
import com.annex.backend.dto.PostRequest;
import com.annex.backend.dto.UserResponse;
import com.annex.backend.models.Follow;
import com.annex.backend.models.Image;
import com.annex.backend.models.User;
import com.annex.backend.repositories.*;
import com.annex.backend.services.mail.CredentialChecker;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final SaveRepository saveRepository;

    private final CredentialChecker credentialChecker;
    private final ImageService imageService;
    private final PropertiesService propertiesService;
    @Transactional
    public UserResponse userToUserDto(User user){
        UserResponse userRes = new UserResponse();

        userRes.setUsername(user.getUsername());
        userRes.setProfilePicture(propertiesService.backendAddress + "api/images/" + user.getProfilePicture().getPath());
        userRes.setBackgroundPicture((user.getBgImage() != null ? propertiesService.backendAddress + "api/images/" + user.getBgImage().getPath() : null));

        userRes.setBiography(user.getBiography());
        userRes.setTotalPosts(postRepository.findAllByUser(user).size());

        userRes.setFollowers(user.getFollowers().size());
        userRes.setFollowing(user.getFollowing().size());
        userRes.setSaved(saveRepository.findByUser(user).size());
        userRes.setLiked((likeRepository.findByUser(user).size()));

        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser"){
            userRes.setFollowed(followRepository.findFollow(getCurrentUser(), user).isPresent());
        }else{
            userRes.setFollowed(false);
        }

        userRes.setLocation(user.getLocation());

        if(user.getBirthday() != null){
            DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
            String strDate = dateFormat.format(user.getBirthday());
            userRes.setBirthday(strDate);
        }

        userRes.setJoinedOn(DateTimeFormatter.ofPattern("MMM dd yyyy").withZone(ZoneId.systemDefault()).format(user.getCreatedAt()));

        return userRes;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @Transactional
    public ResponseEntity<UserResponse> getUserByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found!"));
        UserResponse userRes = userToUserDto(user);

        return new ResponseEntity<UserResponse>(userRes, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<UserResponse> getUserByToken(){
        UserResponse userRes = new UserResponse();
        User user = userRepository.findByEmail(getCurrentUser().getEmail()).orElseThrow(()-> new RuntimeException("User not found"));
        userRes = userToUserDto(user);
        return new ResponseEntity<UserResponse>(userRes, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<List<UserResponse>> getSuggested(){
        Pageable pageable = PageRequest.of(0, 5);
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser"){
            List<UserResponse> suggestedUsers = userRepository.findSuggested(getCurrentUser(), pageable).stream().map(this::userToUserDto).collect(Collectors.toList());
            return new ResponseEntity<List<UserResponse>>(suggestedUsers, HttpStatus.OK);
        }else{
            List<UserResponse> suggestedUsers = userRepository.findAllOrderByFollowing(pageable).stream().map(this::userToUserDto).collect(Collectors.toList());
            return new ResponseEntity<List<UserResponse>>(suggestedUsers, HttpStatus.OK);
        }
    }

    @Transactional(readOnly = true)
    public boolean doesUserExistByUsername(String username){
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean doesUserExistByMail(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public ResponseEntity<String> followUser(String username){
        Follow follow = new Follow();

        follow.setFollower(getCurrentUser());
        follow.setFollowing(userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found!")));
        follow.setFollowedAt(Instant.now());

        followRepository.save(follow);

        return new ResponseEntity<String>("Ok", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<UserResponse> editProfile(String username, MultipartFile profilePicture, MultipartFile backgroundPicture, String jsonString){
        User userToEdit = userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User doesn't exist!"));

        ObjectMapper mapper = new ObjectMapper();

        EditRequest editRequest;

        try{
            editRequest = mapper.readValue(jsonString, EditRequest.class);
        }catch (Exception e){
            throw  new RuntimeException("Invalid request");
        }

        if(!userToEdit.getEmail().equals(getCurrentUser().getEmail())){
            throw new RuntimeException("No permission!");
        }

        if(editRequest.getUsername().length() < 6){
            throw new RuntimeException("Username should be at least 6 characters!");
        }

        if(!credentialChecker.isUsernameValid(editRequest.getUsername())){
            throw new RuntimeException("Username is not valid");
        }

        if(!editRequest.getUsername().equals(userToEdit.getUsername())){
            if(doesUserExistByUsername(editRequest.getUsername())){
                throw new RuntimeException("User with that username already exists!");
            }
            userToEdit.setUsername(editRequest.getUsername());
        }

        userToEdit.setBiography(editRequest.getBiography());

        if(editRequest.getBirthday() != null){
            userToEdit.setBirthday(editRequest.getBirthday());
        }

        userToEdit.setLocation(editRequest.getLocation());

        if(profilePicture != null){
            Image newImage = imageService.uploadImage(profilePicture, getCurrentUser());
            userToEdit.setProfilePicture(newImage);
        }

        if(backgroundPicture != null){
            Image newImage = imageService.uploadImage(backgroundPicture, getCurrentUser());
            userToEdit.setBgImage(newImage);
        }

        UserResponse userRes = userToUserDto(userRepository.save(userToEdit));

        return new ResponseEntity<UserResponse>(userRes, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<List<UserResponse>> searchUsers(String username, Instant startDate){
        Pageable pageable = PageRequest.of(0, 5);
        List<User> users = userRepository.searchUsers(username, startDate, pageable);
        return new ResponseEntity<>(users.stream().map(this::userToUserDto).collect(Collectors.toList()), HttpStatus.OK);
    }

}

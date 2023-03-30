package com.annex.backend.services;

import com.annex.backend.dto.PostRequest;
import com.annex.backend.dto.PostResponse;
import com.annex.backend.models.*;
import com.annex.backend.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PostService {
    @Autowired
    private final TagRepository tagRepository;
    @Autowired
    private final PostRepository postRepository;
    @Autowired
    private final UserService userService;
    @Autowired
    private final LikeRepository likeRepository;
    @Autowired
    private final SaveRepository saveRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ImageService imageService;
    @Autowired
    private final NotificationService notificationService;

    private final PropertiesService propertiesService;

    @Transactional
    public PostResponse postToPostRes(Post post){
        PostResponse postResponse = new PostResponse();

        String message = post.getMessage();

        String patternStr = "(?:\\s|\\A)#+([A-Za-z0-9-_]+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(message);
        String result = "";

        while (matcher.find()) {
            result = matcher.group();
            result = result.replace(" ", "");

            String search = result.replace("#", "");
            String searchHTML="<a id='tag-span' class='post-link' href=/tag/" + search + ">" + result + "</a>";

            message = message.replace(result,searchHTML);
        }

        String patternStr2 = "(?:\\s|\\A)@+([A-Za-z0-9-_]+)";
        Pattern pattern2 = Pattern.compile(patternStr2);
        Matcher matcher2 = pattern2.matcher(message);
        String result2 = "";

        while (matcher2.find()) {
            result2 = matcher2.group();
            result2 = result2.replace(" ", "");
            String search = result2.replace("@", "");
            if(userRepository.findByUsername(search).isPresent()){
                String searchHTML="<a id='mention-span' class='post-link' href=/profile/" + search + ">" + result2 + "</a>";
                message = message.replace(result2,searchHTML);
            }
        }

        postResponse.setId(post.getPostId());

        postResponse.setMessage(message);

        postResponse.setCreator(post.getUser().getUsername());
        postResponse.setCreatorImage(propertiesService.backendAddress + "api/images/" + post.getUser().getProfilePicture().getPath());

        if(post.getImageUrls() != null){
            Vector<String> imgUrls = new Vector<>();

            for(String url : post.getImageUrls()){
                imgUrls.add(propertiesService.backendAddress + "api/images/" + url);
            }

            postResponse.setImageUrls(imgUrls);
        }

        if(post.getReplyingAt() != null){
            postResponse.setReply(true);
            postResponse.setReplyingToUser(post.getReplyingAt().getUser().getUsername());
            postResponse.setReplyingToPost(post.getReplyingAt().getPostId().toString());
        }else{
            postResponse.setReply(false);
        }

        if(post.getLikeList() != null) {
            postResponse.setLikeCount(post.getLikeList().size());
        }else{
            postResponse.setLikeCount(0);
        }

        if(post.getReplies() != null) {
            postResponse.setRepliesCount(post.getReplies().size());
        }else{
            postResponse.setRepliesCount(0);
        }

        if(post.getSaveList()!= null){
            postResponse.setSaveCount(post.getSaveList().size());
        }else{
            postResponse.setSaveCount(0);
        }

        if(post.getCreatedAt().isBefore(Instant.now().minus(364, ChronoUnit.DAYS))){
            postResponse.setCreatedAt(DateTimeFormatter.ofPattern("MMM dd yyyy").withZone(ZoneId.systemDefault()).format(post.getCreatedAt()));
        }else if(post.getCreatedAt().isBefore(Instant.now().minus(24, ChronoUnit.HOURS))){
            postResponse.setCreatedAt(DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault()).format(post.getCreatedAt()) + " at " +
                    DateTimeFormatter.ofPattern("hh:mm").withZone(ZoneId.systemDefault()).format(post.getCreatedAt()));
        }else{
            PrettyTime prettyTime = new PrettyTime();
            postResponse.setCreatedAt(prettyTime.format(Date.from(post.getCreatedAt())));
        }

        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser"){
            User currentUser = userService.getCurrentUser();

            if(likeRepository.findByUserAndPost(currentUser, post).isPresent()){
                postResponse.setLiked(true);
            }else{
                postResponse.setLiked(false);
            }

            if(saveRepository.findByUserAndPost(currentUser, post).isPresent()){
                postResponse.setSaved(true);
            }else{
                postResponse.setSaved(false);
            }

        }else{
            postResponse.setLiked(false);
            postResponse.setSaved(false);
        }

        return postResponse;
    }

    @Transactional
    public ResponseEntity save(String jsonString, MultipartFile[] images){
        ObjectMapper mapper = new ObjectMapper();

        Post newPost = new Post();

        HashSet<String> hashtags = new HashSet<>();
        HashSet<String> users = new HashSet<>();

        try{
            PostRequest postRequest = mapper.readValue(jsonString, PostRequest.class);
            String message = postRequest.getMessage();

            String patternStr = "(?:\\s|\\A)#+([A-Za-z0-9-_]+)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(message);
            String result = "";

            while (matcher.find()) {
                result = matcher.group();
                result = result.replace(" ", "");

                String search = result.replace("#", "");
                hashtags.add(search);
            }

            String patternStr2 = "(?:\\s|\\A)@+([A-Za-z0-9-_]+)";
            Pattern pattern2 = Pattern.compile(patternStr2);
            Matcher matcher2 = pattern2.matcher(message);
            String result2 = "";

            while (matcher2.find()) {
                result2 = matcher2.group();
                result2 = result2.replace(" ", "");
                String search = result2.replace("@", "");
                users.add(search);
                if(userRepository.findByUsername(search).isPresent()){

                }
            }

            newPost.setMessage(message);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        newPost.setCreatedAt(Instant.now());

        newPost.setUser(userService.getCurrentUser());
        newPost.setReply(false);

        if(images != null){
            Vector<String> imageUrls = new Vector<>();

            for(MultipartFile image : images){
                imageUrls.add((imageService.uploadImage(image, userService.getCurrentUser()).getPath()));
            }

            newPost.setImageUrls(imageUrls);
        }

        List<Tag> tagList = new ArrayList<>();

        for(String hash : hashtags){
            Tag tag = null;

            if(tagRepository.findByTagName(hash).isEmpty()){
                tag = new Tag();
                tag.setTagName(hash);
                tag = tagRepository.save(tag);
            }else{
                tag = tagRepository.findByTagName(hash).get();
            }

            tagList.add(tag);

        }

        newPost.setTagList(tagList);

        Post pubblishedPost = postRepository.save(newPost);

        for(String user : users){

        }

        return new ResponseEntity(postToPostRes(pubblishedPost), HttpStatus.CREATED);
    }

    @Transactional
    public PostResponse getPost(Long id){
        Post postFound = postRepository.findById(id).orElseThrow(()-> new IllegalStateException("Post not found!"));
        return postToPostRes(postFound);
    }

    @Transactional
    public List<PostResponse> getAllPosts(int page, Instant startDate){
        Pageable pageable = PageRequest.of(page, 10);
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser"){
            User user = userService.getCurrentUser();
            return postRepository.findMostRecentPostsNotCreatedBy(startDate, user, pageable).stream().map(this::postToPostRes).collect(Collectors.toList());
        }else{
            return postRepository.findMostRecent(startDate, pageable).stream().map(this::postToPostRes).collect(Collectors.toList());
        }
    }

    @Transactional
    public  List<PostResponse> getPostsWithTag(String tag, int page, Instant startDate){
        Pageable pageable = PageRequest.of(0, 15);
        List<Post> postList = postRepository.findByTag(tag, startDate, pageable);
        return postList.stream().map(this::postToPostRes).collect(Collectors.toList());
    }

    @Transactional
    public PostResponse reply(PostRequest replyRequest, Long replyingAt){
        Post reply = new Post();
        Post toReply = postRepository.findById(replyingAt).orElseThrow(()-> new RuntimeException("Post not found"));
        reply.setReplyingAt(toReply);

        reply.setMessage(replyRequest.getMessage());
        reply.setUser(userService.getCurrentUser());
        reply.setCreatedAt(Instant.now());
        reply.setReply(true);

        reply = postRepository.save(reply);

        notificationService.createNotification(toReply.getUser(), userService.getCurrentUser().getUsername() + " replied to your post!",
                propertiesService.backendAddress + "api/images/" + userService.getCurrentUser().getProfilePicture().getPath(), "/post/" + reply.getPostId());

        return postToPostRes(reply);
    }

    @Transactional
    public ResponseEntity<List<PostResponse>> getReplies(Long id, int page, Instant startingDate){
        Pageable pageable = PageRequest.of(page, 10);
        Post post = postRepository.findById(id).orElseThrow(()-> new RuntimeException("Post not found"));
        List<PostResponse> posts = postRepository
                .findAllByReplyingAtAndCreatedAtBeforeOrderByCreatedAtDesc(post, startingDate, pageable)
                .stream().map(this::postToPostRes).collect(Collectors.toList());
        return new ResponseEntity<List<PostResponse>>(posts, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> deletePost(Long id){
        Post post = postRepository.findById(id).orElseThrow(()-> new RuntimeException("Post not found"));

        if(userService.getCurrentUser() != post.getUser() && !userService.getCurrentUser().isAdmin()){
            return new ResponseEntity<String>("No permission!", HttpStatus.BAD_REQUEST);
        }

        if (post.getReplies() != null) {
            for(Post postToDelete : post.getReplies()){
                deletePost(postToDelete.getPostId());
            }
        }

        if (post.getImageUrls() != null) {
            for(String imgUrl : post.getImageUrls()){
                imageService.deleteImageByUrl(imgUrl);
            }
        }

        likeRepository.removeByPost(post);
        saveRepository.removeByPost(post);
        postRepository.removeByReplyingAt(post);
        postRepository.removeByPostId(id);

        return new ResponseEntity<String>("Removed", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<List<PostResponse>> getPostsFromUser(String username, int page, Instant startingDate){
        Pageable pageable = PageRequest.of(page, 10);
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found!"));
        List<PostResponse> posts = postRepository.findAllByUserAndCreatedAtBeforeOrderByCreatedAtDesc(user, pageable, startingDate)
                .stream().map(this::postToPostRes).collect(Collectors.toList());
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<List<PostResponse>> getLikedFromUser(String username, int page, Instant startingDate){
        Pageable pageable = PageRequest.of(page, 10);

        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found!"));
        List<Post> likes = likeRepository.findByUserOrderByCreatedAtDesc(user, pageable).stream().map(LikeVote::getPost).toList();

        return new ResponseEntity<List<PostResponse>>(likes.stream().map(this::postToPostRes).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<List<PostResponse>> getSavedFromUser(String username, int page, Instant startingDate){
        Pageable pageable = PageRequest.of(page, 10);
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found!"));

        List<Save> saves = saveRepository.findByUserOrderByCreatedAtDesc(user, pageable).orElseThrow(()-> new RuntimeException("Not found!"));

        List<Post> postSaves = saves.stream().map(Save::getPost).toList();
        return new ResponseEntity<List<PostResponse>>(postSaves.stream().map(this::postToPostRes).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<List<PostResponse>> searchPosts(String text, int page, Instant startDate){
        Pageable pageable = PageRequest.of(page, 15);
        System.out.println("text:" + text);
        List<Post> posts = postRepository.searchPosts(text, startDate, pageable);
        return new ResponseEntity<List<PostResponse>>(posts.stream().map(this::postToPostRes).collect(Collectors.toList()), HttpStatus.OK);
    }

}

package com.annex.backend.services;

import com.annex.backend.dto.PostRequest;
import com.annex.backend.dto.PostResponse;
import com.annex.backend.dto.CursorPostsResponse;
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
            String searchHTML="<a id='tag-span' class='post-link' href=/search/%23" + search + ">" + result + "</a>";

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
    public PostResponse save(String jsonString, MultipartFile[] images, Long replyingTo){
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
            }

            newPost.setMessage(message);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        newPost.setCreatedAt(Instant.now());
        newPost.setUser(userService.getCurrentUser());

        Post toReply = postRepository.findById(replyingTo).orElseThrow(()-> new RuntimeException("Post not found"));

        if(replyingTo != -1){
            if(toReply.getUser() != userService.getCurrentUser()){
                newPost.setReplyingAt(toReply);
                newPost.setReply(true);
            }
        }else{
            newPost.setReply(false);
        }

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
            Optional<User> reciever = userRepository.findByUsername(user);
            if(reciever.isPresent()){
                User recieverUser = reciever.get();
                notificationService.createNotification(recieverUser, userService.getCurrentUser().getUsername() + "mentioned you in a post!",
                        userService.getCurrentUser().getProfilePicture().getPath(), "/post/" + newPost.getPostId());
            }
        }

        if(replyingTo != -1){
            notificationService.createNotification(toReply.getUser(), userService.getCurrentUser().getUsername() + " replied to your post!",
                    userService.getCurrentUser().getProfilePicture().getPath(), "/post/" + pubblishedPost.getPostId());
        }

        return postToPostRes(pubblishedPost);
    }

    @Transactional
    public PostResponse getPost(Long id){
        Post postFound = postRepository.findById(id).orElseThrow(()-> new IllegalStateException("Post not found!"));
        return postToPostRes(postFound);
    }

    @Transactional
    public List<PostResponse> getAllPosts(Long cursor, int pageSize){

        if(cursor == -1){
            Pageable pageable = PageRequest.of(0, 1);
            cursor = postRepository.findAllMostRecents(pageable).get(0).getPostId();
        }
        Pageable pageable = PageRequest.of(0, pageSize);
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser"){
            User user = userService.getCurrentUser();
            return postRepository.findMostRecentsNotCreatedBy(cursor, user, pageable).stream().map(this::postToPostRes).collect(Collectors.toList());
        }else{
            return postRepository.findMostRecents(cursor, pageable).stream().map(this::postToPostRes).collect(Collectors.toList());
        }
    }

    @Transactional
    public List<PostResponse> getReplies(Long id, Long cursor, int pageSize){
        Post post = postRepository.findById(id).orElseThrow(()-> new RuntimeException("Post not found"));

        if(cursor == -1){
            Pageable pag = PageRequest.of(0, 1);
            List<Post> cursorPosts = postRepository.findAllRepliesToPost(post, pag);
            if(cursorPosts.size() == 0){
                return new ArrayList<>();
            }
            cursor = cursorPosts.get(0).getPostId();
        }

        Pageable pageable = PageRequest.of(0, pageSize);
        List<PostResponse> posts = postRepository
                .findRepliesToPost(post, cursor, pageable)
                .stream().map(this::postToPostRes).collect(Collectors.toList());
        return posts;
    }

    @Transactional
    public List<PostResponse> getPostsFromUser(String username, Long cursor, int pageSize){
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found!"));
        if(cursor == -1){
            Pageable pag = PageRequest.of(0, 1);
            List<Post> cursorPosts = postRepository.findAllByUserPag(user, pag);
            if(cursorPosts.size() == 0){
                return new ArrayList<>();
            }
            cursor = cursorPosts.get(0).getPostId();
        }
        Pageable pageable = PageRequest.of(0, pageSize);
        List<PostResponse> posts = postRepository.findByUser(user, cursor, pageable)
                .stream().map(this::postToPostRes).collect(Collectors.toList());
        return posts;
    }

    @Transactional
    public CursorPostsResponse getLikedFromUser(String username, Long cursor, int pageSize){
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found!"));

        CursorPostsResponse cursorPostsResponse = new CursorPostsResponse();

        if(cursor == -1){
            Pageable pag = PageRequest.of(0, 1);
            List<LikeVote> cursorLikes = likeRepository.findAllByUserPag(user, pag);
            if(cursorLikes.size() == 0){
                cursorPostsResponse.setPosts(new ArrayList<>());
                cursorPostsResponse.setCursor(1L);
                return cursorPostsResponse;
            }
            cursor = cursorLikes.get(0).getLikeId();
        }
        Pageable pageable = PageRequest.of(0, pageSize);

        List<LikeVote> likes = likeRepository.findAllByUser(user, cursor, pageable);
        List<PostResponse> posts = likes.stream().map(LikeVote::getPost).toList().stream().map(this::postToPostRes).collect(Collectors.toList());

        if(likes.size() == 0){
            cursorPostsResponse.setCursor(1L);
        }else{
            cursorPostsResponse.setCursor(likes.get(likes.size()-1).getLikeId());
        }

        cursorPostsResponse.setPosts(posts);

        return cursorPostsResponse;
    }

    @Transactional
    public CursorPostsResponse getSavedFromUser(String username, Long cursor, int pageSize){
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found!"));

        CursorPostsResponse cursorPostsResponse = new CursorPostsResponse();

        if(cursor == -1){
            Pageable pag = PageRequest.of(0, 1);
            List<Save> cursorSaves = saveRepository.findAllByUserPag(user, pag);
            if(cursorSaves.size() == 0){
                cursorPostsResponse.setPosts(new ArrayList<>());
                cursorPostsResponse.setCursor(1L);
                return cursorPostsResponse;
            }
            cursor = cursorSaves.get(0).getSaveId();
        }
        Pageable pageable = PageRequest.of(0, pageSize);

        List<Save> saves = saveRepository.findAllByUser(user, cursor, pageable);

        List<PostResponse> postSaves = saves.stream().map(Save::getPost).toList().stream().map(this::postToPostRes).collect(Collectors.toList());

        if(saves.size() == 0){
            cursorPostsResponse.setCursor(1L);
        }else{
            cursorPostsResponse.setCursor(saves.get(postSaves.size()-1).getSaveId());
        }

        cursorPostsResponse.setPosts(postSaves);
        return cursorPostsResponse;
    }

    @Transactional
    public List<PostResponse> searchPosts(String text, Long cursor, int pageSize){
        if(cursor == -1){
            Pageable pag = PageRequest.of(0, 1);
            List<Post> cursorPosts = postRepository.findAllByMessagePag(text, pag);
            if(cursorPosts.size() == 0){
                return new ArrayList<>();
            }
            cursor = cursorPosts.get(0).getPostId();
        }
        Pageable pageable = PageRequest.of(0, pageSize);
        List<Post> posts = postRepository.findAllByMessage(text, cursor, pageable);
        return posts.stream().map(this::postToPostRes).collect(Collectors.toList());
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
}

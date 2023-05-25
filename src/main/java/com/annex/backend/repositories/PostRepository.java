package com.annex.backend.repositories;

import com.annex.backend.models.Post;
import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT p FROM Post p ORDER BY p.postId DESC")
    List<Post> findAllMostRecents(Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.postId < :cursor ORDER BY p.postId DESC")
    List<Post> findMostRecents(Long cursor, Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.user <> :notCreator AND p.postId < :cursor ORDER BY p.postId DESC")
    List<Post> findMostRecentsNotCreatedBy(Long cursor, User notCreator, Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.replyingAt = :post ORDER BY p.postId DESC")
    List<Post> findAllRepliesToPost(Post post, Pageable pageable);
    @Query(value = "SELECT p FROM Post p WHERE p.replyingAt = :post AND p.postId < :cursor ORDER BY p.postId DESC")
    List<Post> findRepliesToPost(Post post, Long cursor, Pageable pageable);

    List<Post> findAllByUser(User user);
    @Query(value = "SELECT p FROM Post p WHERE p.user = :user ORDER BY p.postId DESC")
    List<Post> findAllByUserPag(User user, Pageable pageable);
    @Query(value = "SELECT p FROM Post p WHERE p.user = :user AND p.postId < :cursor ORDER BY p.postId DESC")
    List<Post> findByUser(User user, Long cursor, Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.message LIKE CONCAT('%', :text, '%') AND p.postId < :cursor ORDER BY p.postId DESC")
    List<Post> findAllByMessage(String text, Long cursor, Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.message LIKE CONCAT('%', :text, '%') ORDER BY p.postId DESC")
    List<Post> findAllByMessagePag(String text, Pageable pageable);
    List<Post> removeByPostId(Long id);
    List<Post> removeByReplyingAt(Post post);

}

package com.annex.backend.repositories;

import com.annex.backend.models.Post;
import com.annex.backend.models.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT p FROM Post p join p.likeList l WHERE l.createdAt >= :date AND p.user <> :notCreator AND p.createdAt <= :startDate ORDER BY p.likeList.size DESC")
    List<Post> findMostPopularNotCreatedBy(Instant date, Instant startDate, User notCreator, Pageable pageable);

    @Query(value = "SELECT p FROM Post p join p.likeList l WHERE l.createdAt >= :date AND p.createdAt <= :startDate ORDER BY p.likeList.size DESC")
    List<Post> findMostPopular(Instant date, Instant startDate, Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.createdAt <= :startDate ORDER BY p.createdAt DESC")
    List<Post> findMostRecent(Instant startDate, Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.createdAt <= :startDate AND p.user <> :notCreator AND p.isReply = false ORDER BY p.createdAt DESC")
    List<Post> findMostRecentPostsNotCreatedBy(Instant startDate, User notCreator, Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN p.tagList t WHERE t.tagName = :tag AND p.createdAt <= :startDate")
    List<Post> findByTag(String tag, Instant startDate, Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE p.message LIKE CONCAT('%', :text, '%') AND p.createdAt <= :startDate")
    List<Post> searchPosts(String text, Instant startDate, Pageable pageable);

    List<Post> findAllByReplyingAtAndCreatedAtBeforeOrderByCreatedAtDesc(Post post, Instant startingDate, Pageable pageable);

    List<Post> findAllByUser(User user);
    List<Post> findAllByUserAndCreatedAtBeforeOrderByCreatedAtDesc(User user, Pageable pageable, Instant startingDate);

    List<Post> removeByPostId(Long id);
    List<Post> removeByReplyingAt(Post post);


}

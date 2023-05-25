package com.annex.backend.repositories;

import com.annex.backend.models.LikeVote;
import com.annex.backend.models.Post;
import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeVote, Long> {
    List<LikeVote> findByUser(User user);
    @Query(value = "SELECT l FROM LikeVote l WHERE l.user = :user AND l.likeId < :cursor ORDER BY l.likeId DESC")
    List<LikeVote> findAllByUser(User user, Long cursor, Pageable pageable);
    @Query(value = "SELECT l FROM LikeVote l WHERE l.user = :user ORDER BY l.likeId DESC")
    List<LikeVote> findAllByUserPag(User user, Pageable pageable);
    Optional<LikeVote> findByUserAndPost(User user, Post post);
    List<LikeVote> removeByLikeId(Long id);
    List<LikeVote> removeByPost(Post post);
}

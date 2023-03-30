package com.annex.backend.repositories;

import com.annex.backend.models.LikeVote;
import com.annex.backend.models.Post;
import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeVote, Long> {
    Optional<LikeVote> findByUserAndPost(User user, Post post);
    List<LikeVote> findByUser(User user);
    List<LikeVote> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<LikeVote> removeByLikeId(Long id);
    List<LikeVote> removeByPost(Post post);
}

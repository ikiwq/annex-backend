package com.annex.backend.repositories;

import com.annex.backend.models.Post;
import com.annex.backend.models.Save;
import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaveRepository extends JpaRepository<Save, Long> {
    List<Save> findByUser(User user);
    Optional<List<Save>> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<Save> findByUserAndPost(User user, Post post);
    void removeBySaveId(Long id);
    void removeByPost(Post post);
}

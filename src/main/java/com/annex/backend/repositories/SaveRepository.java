package com.annex.backend.repositories;

import com.annex.backend.models.Post;
import com.annex.backend.models.Save;
import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SaveRepository extends JpaRepository<Save, Long> {
    List<Save> findByUser(User user);
    @Query(value = "SELECT s FROM Save s WHERE s.user = :user ORDER BY s.saveId DESC")
    List<Save> findAllByUserPag(User user, Pageable pageable);
    @Query(value = "SELECT s FROM Save s WHERE s.user = :user AND s.id <= :cursor ORDER BY s.saveId DESC")
    List<Save> findAllByUser(User user, Long cursor, Pageable pageable);
    Optional<Save> findByUserAndPost(User user, Post post);
    void removeBySaveId(Long id);
    void removeByPost(Post post);
}

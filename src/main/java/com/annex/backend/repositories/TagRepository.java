package com.annex.backend.repositories;

import com.annex.backend.models.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);

    @Query(value = "SELECT t from Tag t join t.postList p WHERE p.createdAt >= :date AND t.postList.size > 0 GROUP by t ORDER BY t.postList.size DESC")
    List<Tag> findMostPopular(Instant date, Pageable pageable);

    @Query(value = "SELECT t from Tag t WHERE t.postList.size > 0 ORDER BY t.postList.size DESC")
    List<Tag> findWithMostLikes(Pageable pageable);
}

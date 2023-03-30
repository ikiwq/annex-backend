package com.annex.backend.repositories;

import com.annex.backend.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByPath(String path);
    List<Image> deleteByPath(String path);
}

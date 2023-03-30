package com.annex.backend.repositories;

import com.annex.backend.models.Follow;
import com.annex.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    @Query(value = "SELECT f from Follow f join f.follower flw join f.following flwng WHERE flw = :follower AND flwng = :following")
    Optional<Follow> findFollow(User follower, User following);
}

package com.annex.backend.repositories;

import com.annex.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u order by u.followers.size")
    List<User> findAllOrderByFollowing(Pageable pageable);

    @Query("SELECT user from User user WHERE NOT EXISTS (SELECT us from User u join u.following fl JOIN fl.following us WHERE u = :user AND user = us) AND user <> :user")
    List<User> findSuggested(User user, Pageable pageable);

    @Query("SELECT us from User u join u.following fl JOIN fl.following us WHERE u = :user")
    List<User> findFollowed(User user);

    @Query("SELECT u from User u WHERE u.username LIKE %:username% AND u.createdAt <= :createdAt")
    List<User> searchUsers(String username, Instant createdAt, Pageable pageable);

    Optional<User> findByEmail(String mail);
    Optional<User> findByUsername(String username);
}

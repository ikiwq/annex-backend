package com.annex.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long FollowId;

    @NotNull
    @OneToOne
    @JoinColumn(name = "followerId", referencedColumnName = "userId")
    private User follower;

    @NotNull
    @OneToOne
    @JoinColumn(name = "followingId", referencedColumnName = "userId")
    private User following;

    private Instant followedAt;
}

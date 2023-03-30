package com.annex.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"User\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @ManyToOne
    private Image profilePicture;

    @OneToOne
    @Nullable
    private Image bgImage;

    private String biography;

    @Nullable
    private Date dateOfBirth;

    private String location;
    private Instant createdAt;
    private Date birthday;

    private boolean isLocked;
    private boolean isEnabled;
    private boolean isAdmin;

    @OneToMany(mappedBy = "recipient")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "following")
    private List<Follow> followers;

    @OneToMany(mappedBy = "follower")
    private List<Follow> following;

    @OneToMany(mappedBy = "user")
    private List<LikeVote> likeList;

    @OneToMany(mappedBy = "user")
    private List<Post> postList;

    @OneToMany(mappedBy = "user")
    private List<Image> imageList;

}

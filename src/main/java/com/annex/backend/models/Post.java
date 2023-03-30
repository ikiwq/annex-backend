package com.annex.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Nullable
    @OneToMany(mappedBy = "replyingAt")
    private List<Post> replies;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replying_at", referencedColumnName = "postId", nullable = true)
    private Post replyingAt;

    private boolean isReply;

    @NotBlank(message = "Post message cannot be empty")
    private String message;

    @Nullable
    private String postUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    User user;

    private Integer voteCount;

    private Instant createdAt;

    @Nullable
    @Lob
    @Column(length = 1000)
    private Vector<String> imageUrls;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "post_tag", joinColumns = @JoinColumn(name = "postId"), inverseJoinColumns = @JoinColumn(name = "tagId"))
    private List<Tag> tagList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<LikeVote> likeList;

    @OneToMany(mappedBy = "post")
    private List<Save> saveList;
}

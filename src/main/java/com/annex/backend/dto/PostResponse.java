package com.annex.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Vector;

@Data
@NoArgsConstructor
public class PostResponse {
    private Long id;

    private String message;

    private String creator;
    private String creatorImage;

    private Vector<String> imageUrls;

    private boolean isReply;
    private String replyingToUser;
    private String replyingToPost;

    private String createdAt;

    private Integer likeCount;
    private Integer repliesCount;
    private Integer saveCount;

    private boolean liked;
    private boolean saved;
}

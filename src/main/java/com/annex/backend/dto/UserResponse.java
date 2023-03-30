package com.annex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String username;

    private String profilePicture;
    private String backgroundPicture;

    private String biography;
    private String joinedOn;
    private String location;
    private String birthday;

    private boolean isFollowed;

    private int followers;
    private int following;
    private int totalPosts;
    private int liked;
    private int saved;


}

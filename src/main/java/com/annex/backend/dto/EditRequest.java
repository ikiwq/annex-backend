package com.annex.backend.dto;

import lombok.Data;

import java.util.Date;

@Data
public class EditRequest {
    String username;
    String biography;
    String location;
    Date birthday;
}

package com.annex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String text;
    private String imageUrl;
    private String toUrl;
    private boolean seen;
}

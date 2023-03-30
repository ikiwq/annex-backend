package com.annex.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User recipient;

    private String text;
    private String imageUrl;
    private String toUrl;

    private boolean seen;

    private Instant createdAt;
}

package com.annex.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String path;
    private String type;

    @ManyToOne
    @JoinColumn(name = "uploader", referencedColumnName = "userId")
    private User user;

    private Instant uplaodedAt;

    @Lob
    private byte[] data;
}

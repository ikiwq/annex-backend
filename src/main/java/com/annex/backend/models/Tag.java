package com.annex.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @NotBlank(message = "Tag name is required!")
    private String tagName;

    private Integer postNumber;

    @ManyToMany(mappedBy = "tagList")
    private List<Post> postList;
}

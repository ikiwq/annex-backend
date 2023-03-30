package com.annex.backend.services;

import com.annex.backend.dto.TagDto;
import com.annex.backend.models.Tag;
import com.annex.backend.repositories.TagRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagDto> getAll(){
        return tagRepository.findAll().stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TagDto> getPopular(){
        int numberOfTags = 10;
        Pageable pageable = PageRequest.of(0, numberOfTags);
        List<TagDto> tagList = tagRepository.findMostPopular(Instant.now().minus(24, ChronoUnit.HOURS), pageable).stream().map(this::mapToDto).collect(Collectors.toList());
        if(tagList.size() < numberOfTags){
            int compensate = numberOfTags - tagList.size();
            Pageable pag = PageRequest.of(0, compensate);
            List<TagDto> secondTagList = tagRepository.findWithMostLikes(pag).stream().map(this::mapToDto).collect(Collectors.toList());
            for(TagDto tag : secondTagList){
                if(!tagList.contains(tag)){
                    tagList.add(tag);
                }
            }
        }
        return tagList;
    }

    private TagDto mapToDto(Tag tag) {
        TagDto newTagDto = new TagDto();

        newTagDto.setId(tag.getTagId());
        newTagDto.setName(tag.getTagName());
        newTagDto.setPostNumber(tag.getPostList().size());

        return newTagDto;
    }


    @Transactional
    public TagDto save (TagDto tagDto){
        Tag newTag = mapTagDto(tagDto);
        tagRepository.save(newTag);
        tagDto.setId(newTag.getTagId());
        return tagDto;
    }

    private Tag mapTagDto(TagDto tagDto){
        Tag newTag = new Tag();
        newTag.setTagName(tagDto.getName());
        return newTag;
    }
}

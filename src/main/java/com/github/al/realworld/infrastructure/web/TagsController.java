package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.domain.Tag;
import com.github.al.realworld.domain.TagRepository;
import com.github.al.realworld.infrastructure.web.dto.TagsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tags")
public class TagsController {

    private final TagRepository tagRepository;

    @GetMapping
    public TagsDto findAll() {
        List<String> tagList = StreamSupport.stream(tagRepository.findAll().spliterator(), false)
                .map(Tag::getName)
                .collect(Collectors.toList());
        return new TagsDto(tagList);
    }

}

package com.postify.main.controllers;

import com.postify.main.dto.TagDto.TagRequestDto;
import com.postify.main.dto.TagDto.TagResponseDto;
import com.postify.main.entities.Tag;
import com.postify.main.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tags")
public class   TagController {

    @Autowired
    TagService tagService;

    @PutMapping("/{name}")
    public ResponseEntity<Tag> updateTagByName(@PathVariable String name, @RequestBody TagRequestDto tagRequestDto) {
        Tag updatedTag = tagService.updateByName(name, tagRequestDto);
        return new ResponseEntity<>(updatedTag, HttpStatus.OK);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteByName(@PathVariable String name) {
        tagService.deleteTagByName(name);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAllTags() {
        return new ResponseEntity<List<TagResponseDto>>(tagService.getAll().stream().map(tag ->
                tagService.convertToTagResponseDto(tag)).collect(Collectors.toList()), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(@RequestBody TagRequestDto tagRequestDto) {
        Tag tag = tagService.convertToTag(tagRequestDto);
        Tag createdTag = tagService.create(tag);
        return new ResponseEntity<>(tagService.convertToTagResponseDto(createdTag), HttpStatus.CREATED);
    }
}

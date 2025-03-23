package com.postify.main.services;

import com.postify.main.dto.TagDto.TagRequestDto;
import com.postify.main.dto.TagDto.TagResponseDto;
import com.postify.main.entities.Post;
import com.postify.main.entities.Tag;
import com.postify.main.repository.PostRepository;
import com.postify.main.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TagService {
    @Autowired
    PostRepository postRepository;
    @Autowired
    TagRepository tagRepository;

    public Tag updateByName(String name, TagRequestDto tagRequestDto){
        String newName = tagRequestDto.getName();
        Tag tagName = tagRepository.findByName(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Tag with name "+name+" not found"));
        tagName.setName(newName);
        return tagRepository.save(tagName);
    }

    public  void deleteTagByName(String name){
        Tag tag = tagRepository.findByName(name).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"Tag with name "+name+" not found"));
        Page<Post> posts =  postRepository.findByTagsName(name, Pageable.unpaged());
        if (!posts.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag with name "+name+" has post reference so you cannot delete it");
        }
        tagRepository.deleteByName(name);
    }

    public List<Tag> getAll(){
        return tagRepository.findAll();
    }

    public Tag create(Tag tag){
//        Check if tag exists or not
        if(tagRepository.findByName(tag.getName()).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag with name "+tag.getName()+" is already exist");
        }
        return tagRepository.save(tag);
    }
    public TagResponseDto convertToTagResponseDto(Tag tag){
        return  new TagResponseDto(tag.getId(),tag.getName());
    }

    public Tag convertToTag(TagRequestDto tagRequestDto){
        return  new Tag(0,tagRequestDto.getName());
    }











}

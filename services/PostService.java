package com.postify.main.services;

import com.postify.main.dto.postdto.PostPartialRequestDto;
import com.postify.main.dto.postdto.PostRequestDto;
import com.postify.main.dto.postdto.PostResponseDto;
import com.postify.main.entities.Post;
import com.postify.main.entities.Tag;
import com.postify.main.entities.User;
import com.postify.main.repository.PostRepository;
import com.postify.main.repository.TagRepository;
import com.postify.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service  // This annotation defines business logic
public class PostService {

    @Autowired
    UserService userService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    public Post create(Post post, int userId) {
        User user=userService.getById(userId);
        post.setUser(user);
        post.setTags(getPersistedTags(post.getTags()));
        Post savepost=postRepository.save(post);
        emailService.sendEmail(user.getEmail(),
                "Post creation",
                "your post with title " +post.getTitle()+ " has been created sucesfully on:" +post.getCreatedDate());


        return savepost;
    }

    public Post getPost(int id) {
        return postRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with id " + id + " not found")
        );

    }

    @Transactional
    public Post update(int id, Post updatePost, int userId) {
        User user = userService.getById(userId);
        Post userPost = user.getPosts().stream()
                .filter(post -> post.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Post with id "+id+" not found"));

        if (updatePost.getTitle() != null){
            userPost.setTitle(updatePost.getTitle());
        }
        if (updatePost.getDescription() !=null){
            userPost.setDescription(updatePost.getDescription());
        }
        if (!updatePost.getTags().isEmpty()){
            userPost.setTags(getPersistedTags(updatePost.getTags()));
        }
        return postRepository.save(userPost);
    }
    private Set<Tag> getPersistedTags(Set<Tag> tags) {
        if(tags == null || tags.isEmpty()){
            return new HashSet<>();
        }
        return tags.stream()
                .map(tag -> tagRepository.findByName(tag.getName()).orElseGet(() -> tagRepository.save(tag)))
                .collect(Collectors.toSet());
    }

    @Transactional
    public void delete(int id, int userId) {
       User user = userService.getById(userId);
       Post userPost = user.getPosts().stream().filter(post -> post.getId() == id)
               .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                       "Post with id "+id+" not found"));
       userPost.setTags(new HashSet<>());

       user.getPosts().removeIf(post -> post.getId() == id);

       userRepository.save(user);

       postRepository.deleteById(id);
    }

    public Page<Post> getAll(int page, int size, String sortDirection, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection),sortBy));
        return postRepository.findAll(pageable);
    }

    public Page<Post>searchPosts(int page,int size ,String sortDirection, String sortBy,String search){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection),sortBy));
        return  postRepository.searchPosts(search,pageable);
    }

    public Page<Post> getByTitle(String title,int page, int size, String sortDirection, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection),sortBy));
        return postRepository.findByTitleContaining(title,pageable);
    }

    public Page<Post> getByTag(String tag,int page, int size, String sortDirection, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection),sortBy));
        return postRepository.findByTagsName(tag,pageable);
    }

    public List<Post> getUserPosts(int userId){
        User user = userService.getById(userId);
        return  user.getPosts();
    }

    public PostResponseDto convertToPostResponseDto(Post post) {
       PostResponseDto postResponseDto = new PostResponseDto();
       postResponseDto.setId(post.getId());
       postResponseDto.setTitle(post.getTitle());
       postResponseDto.setDescription(post.getDescription());
       postResponseDto.setCreatedDate(post.getCreatedDate());
       postResponseDto.setLastModifiedDate(post.getLastModifiedDate());
       postResponseDto.setTags(post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));
       postResponseDto.setAuthor(post.getUser().getUsername());
       return  postResponseDto;
    }

    public Post convertToPost(PostRequestDto postRequestDto) {
        Post post = new Post();
        post.setTitle(postRequestDto.getTitle());
        post.setDescription(postRequestDto.getDescription());
        post.setTags( postRequestDto.getTags().stream().map(name -> new Tag(name)).collect(Collectors.toSet()));
        return post;

    }

    public Post convertToPost(PostPartialRequestDto postPartialRequestDto){
        Post post = new Post();
        post.setTitle(postPartialRequestDto.getTitle());
        post.setDescription(postPartialRequestDto.getDescription());
        post.setTags(postPartialRequestDto.getTags().stream().map(Tag::new).collect(Collectors.toSet()));
        return  post;
    }
}

package com.postify.main.services;

import com.postify.main.dto.UserDto.UserPartialRequestDto;
import com.postify.main.dto.UserDto.UserRequestDto;
import com.postify.main.dto.UserDto.UserResponseDto;
import com.postify.main.dto.postdto.PostPartialRequestDto;
import com.postify.main.entities.Post;
import com.postify.main.entities.Role;
import com.postify.main.entities.Tag;
import com.postify.main.entities.User;
import com.postify.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RedisService redisService;
  private final RoleService roleService;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

    public UserService(UserRepository userRepository, RedisService redisService, RoleService roleService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.redisService = redisService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PreAuthorize("permitAll()")
    public User create(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if(existingUser.isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User with username "+user.getUsername()+" is already exist!!");
        }
        Role role = roleService.findByName("ROLE_USER");
        user.setRoles(Set.of(role));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return  userRepository.save(user);
    }

    public User createSuperUser(String username, String password, String email){
        roleService.createAdminRole();

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User already exist.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        Role adminRole = roleService.findByName("ROLE_ADMIN");

        user.setRoles(Set.of(adminRole));

        return  userRepository.save(user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<User> getAll(int page, int size, String sortDirection, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection),sortBy));
        return userRepository.findAll(pageable);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    public  User getById(int id){
        String redisKey = "User:"+id;
        User user = redisService.retrieveData(redisKey, User.class);
        if (user != null){
            return  user;
        }
        user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"User with id "+id+" not found"));
        redisService.storeData(redisKey, user, 1L, TimeUnit.MINUTES);
        return  user;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteById(int id){
        getById(id);
        userRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public User updateById(int id , User user){
        User existingUser = getById(id);
        if (user.getUsername() != null){
            existingUser.setUsername(user.getUsername());
        }
        if (user.getPassword() != null){
            existingUser.setPassword(user.getPassword());
        }
        if (user.getEmail() != null){
            existingUser.setEmail(user.getEmail());
        }
        return  userRepository.save(existingUser);
    }


    public User convertToUser(UserRequestDto userRequestDto){
        User user = new User();
        user.setUsername(userRequestDto.getUsername());
        user.setPassword(userRequestDto.getPassword());
        user.setEmail(userRequestDto.getEmail());
        return user;
    }

    public UserResponseDto converToUserResponseDto(User user){
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        System.out.println(user.getUsername());
        userResponseDto.setUsername(user.getUsername());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setCreatedDate(user.getCreatedDate());
        userResponseDto.setLastModifiedDate(user.getLastModifiedDate());
        userResponseDto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return  userResponseDto;
    }

    public User convertToUser(UserPartialRequestDto userPartialRequestDto){
        User user = new User();
        user.setUsername(userPartialRequestDto.getUsername());
        user.setPassword(userPartialRequestDto.getPassword());
        user.setEmail(userPartialRequestDto.getEmail());
        return user;
    }
}

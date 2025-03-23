package com.postify.main.services;

import com.postify.main.dto.roledto.RoleRequestDto;
import com.postify.main.entities.Role;
import com.postify.main.entities.User;
import com.postify.main.repository.RoleRepository;
import com.postify.main.repository.UserRepository;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public void createAdminRole(){
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()){
            Role adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }
    }

    public Role findByName(String name){
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Role not found "+name));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Role createRole(Role role){
        if (roleRepository.findByName(role.getName()).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Role: "+role.getName()+" Already Exist");
        }
        return roleRepository.save(role);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Set<Role> setRole(RoleRequestDto roleRequestDto){
        User user = userRepository.findById(roleRequestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found with id "+roleRequestDto.getUserId()));

        Role role = roleRepository.findById(roleRequestDto.getRoleId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Role not found"));

        if (user.getRoles().contains(role)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User already have same role");
        }
        user.getRoles().add(role);
        userRepository.save(user);
        return user.getRoles();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public  User getById(int id){
        User user = new User();
        user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"User with id "+id+" not found"));
        return  user;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Set<Role> getAllUserByRoleId(){
       return roleRepository.findAll().stream().map(role -> roleRepository.save(role)).collect(Collectors.toSet());
    }

    public Set<Role>  removeRolesFromUser(RoleRequestDto roleRequestDto){
        User user = userRepository.findById(roleRequestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));

        Role rolesToRemove = roleRepository.findById(roleRequestDto.getRoleId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND," Role Not found"));
        boolean hasRoleToRemove = user.getRoles().remove(rolesToRemove);

        if (!hasRoleToRemove){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST,"User does not have the specified roles");
        }

        userRepository.save(user);

        return user.getRoles();
    }
}

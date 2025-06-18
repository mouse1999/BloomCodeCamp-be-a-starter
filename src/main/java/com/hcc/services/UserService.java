package com.hcc.services;

import com.hcc.converter.Converter;
import com.hcc.dtos.request.userdto.SignUpRequest;
import com.hcc.entities.Authority;
import com.hcc.entities.User;
import com.hcc.enums.AuthorityEnum;
import com.hcc.exceptions.UserNotFoundException;
import com.hcc.exceptions.rolexceptions.InvalidRoleException;
import com.hcc.exceptions.userexceptions.UserAlreadyExistsException;
import com.hcc.models.UserModel;
import com.hcc.repositories.AuthorityRepository;
import com.hcc.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  AuthorityRepository authorityRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;


    public UserModel registerUser(SignUpRequest request) {

        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        User user = User.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String roleWithPrefix = "ROLE_" + request.getRole().toUpperCase();
        AuthorityEnum authorityEnum;

        try {
            authorityEnum = AuthorityEnum.valueOf(roleWithPrefix);
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Invalid role. Valid roles: " +
                    Arrays.stream(AuthorityEnum.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));
        }


        Authority authority = Authority.builder()
                .authority(authorityEnum)
                .user(user)
                .build();

        authorityRepository.save(authority);
        return Converter.toUserModel(userRepository.findByUserName(request.getUserName()).orElse(user));
    }

    public UserModel getUserByUserName(String username) {
        User user  = userRepository.findByUserName(username)
                .orElseThrow(()-> new UserNotFoundException("username not found"));

        return Converter.toUserModel(user);
    }

    public boolean userExists(String username) {
        return userRepository.existsByUserName(username);
    }


    public void deleteUser(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(()-> new UserNotFoundException("this username is not found"));
        userRepository.delete(user);
    }

}
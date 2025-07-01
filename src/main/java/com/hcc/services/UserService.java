package com.hcc.services;

import com.hcc.converter.Converter;
import com.hcc.dtos.request.userdto.SignUpRequest;
import com.hcc.entities.Authority;
import com.hcc.entities.User;
import com.hcc.enums.AuthorityEnum;
import com.hcc.exceptions.userexceptions.UserNotFoundException;
import com.hcc.exceptions.rolexceptions.InvalidRoleException;
import com.hcc.exceptions.userexceptions.UserAlreadyExistsException;
import com.hcc.models.UserModel;
import com.hcc.repositories.AuthorityRepository;
import com.hcc.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Transactional
    public UserModel registerUser(SignUpRequest request) {
        logger.debug("Attempting to register user: {}", request.getUserName());

        if (userRepository.existsByUserName(request.getUserName())) {
            logger.warn("Registration attempt with existing username: {}", request.getUserName());
            throw new UserAlreadyExistsException("Username already exists");
        }

        User user = createUser(request);
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());

        // Create and assign authority
        AuthorityEnum authority = parseAndValidateRole(request.getRole());
        createUserAuthority(user, authority);

        return Converter.toUserModel(fetchUserWithAuthorities(user.getUsername()));
    }

    // --- Helper Methods ---------------------------

    private User createUser(SignUpRequest request) {
        return User.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
    }

    private AuthorityEnum parseAndValidateRole(String role) {
        String roleWithPrefix = "ROLE_" + role.toUpperCase();
        try {
            return AuthorityEnum.valueOf(roleWithPrefix);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role attempted: {}", role);
            throw new InvalidRoleException("Invalid role. Valid roles: " +
                    Arrays.stream(AuthorityEnum.values())
                            .map(Enum::name)
                            .map(name -> name.replace("ROLE_", ""))
                            .collect(Collectors.joining(", ")));
        }
    }

    private void createUserAuthority(User user, AuthorityEnum authorityEnum) {
        Authority authority = Authority.builder()
                .authority(authorityEnum)
                .user(user)
                .build();
        authorityRepository.save(authority);
        logger.debug("Authority {} assigned to user {}", authorityEnum, user.getUsername());
    }

    private User fetchUserWithAuthorities(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    logger.error("Newly registered user not found: {}", username);
                    return new IllegalStateException("User registration failed");
                });
    }

    //--------------------------------------------------------------------------------------------------

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
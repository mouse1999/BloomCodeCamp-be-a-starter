package com.hcc.controllers;

import com.hcc.dtos.request.userdto.JwtResponse;
import com.hcc.dtos.request.userdto.SignUpRequest;
import com.hcc.dtos.request.userdto.TokenValidationResponse;
import com.hcc.models.UserModel;
import com.hcc.repositories.UserRepository;
import com.hcc.services.UserDetailsServiceImpl;
import com.hcc.services.UserService;
import com.hcc.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("api/auth")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest request) {
        UserModel userModel = userService.registerUser(request);

        return ResponseEntity.ok(userModel);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUserName(@PathVariable String username) {
        UserModel userModel = userService.getUserByUserName(username);

        return ResponseEntity.ok(userModel);
    }


    @GetMapping
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        UserDetails userDetails = resolveUserDetails(authentication.getPrincipal());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(new JwtResponse(
                null,
                userDetails.getUsername(),
                roles
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent()
                .build();

    }

    @GetMapping("/validate")
    public ResponseEntity<?> validationEndpoint(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, "Authorization header must be provided in 'Bearer <token>' format."));
        }

        String token = authorizationHeader.substring(7);

        if (jwtUtils.validateJwtToken(token)) {
            return ResponseEntity
                    .ok(new TokenValidationResponse(true, "Token is valid."));
        } else {
            // If validation fails (e.g., expired, invalid signature, malformed payload)
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, "Token is invalid or expired."));
        }
    }


    private UserDetails resolveUserDetails(Object principal) {
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        } else if (principal instanceof String) {
            return userDetailsService.loadUserByUsername((String) principal);
        }
        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }


}

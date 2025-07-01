package com.hcc.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.dtos.request.userdto.JwtResponse;
import com.hcc.dtos.request.userdto.SignInRequest;
import com.hcc.dtos.request.userdto.SignUpRequest;
import com.hcc.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    private final AuthenticationManager authenticationManager;
    @Autowired
    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        // 1. Get request body safely
        String requestBody;
        try {
            requestBody = request.getReader()
                    .lines()
                    .collect(Collectors.joining());
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to read request body", e);
        }

        // 2. Validate JSON structure
        if (!StringUtils.hasText(requestBody)) {
            throw new AuthenticationCredentialsNotFoundException("Empty request body");
        }

        // 3. Parse with proper error handling
        SignInRequest signInRequest;
        try {
            signInRequest = new ObjectMapper().readValue(requestBody, SignInRequest.class);
        } catch (JsonProcessingException e) {
            throw new AuthenticationServiceException("Invalid JSON format", e);
        }

        // 4. Validate credentials presence
        if (!StringUtils.hasText(signInRequest.getUsername()) ||
                !StringUtils.hasText(signInRequest.getPassword())) {
            throw new AuthenticationCredentialsNotFoundException("Missing credentials");
        }

        // 5. Authenticate
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getUsername(),
                        signInRequest.getPassword()
                )
        );
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication
    ) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Build response
        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );

        // Write JSON response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(jwtResponse));
        response.addHeader("Authorization", "Bearer " + jwt);
    }


}

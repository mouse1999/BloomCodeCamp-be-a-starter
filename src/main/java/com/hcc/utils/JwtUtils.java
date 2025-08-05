package com.hcc.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private final Key jwtSecret;

    private final int jwtExpirationMs;

    private UserDetailsService userDetailsService;

    public JwtUtils(String jwtSecret, int jwtExpirationMs, UserDetailsService userDetailsService) {
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
        this.userDetailsService = userDetailsService;

    }

    public String generateJwtToken(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication principal cannot be null");
        }

        final String username;
        final List<String> roles;

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
            roles = extractAuthorities(userDetails.getAuthorities());
        } else if (authentication.getPrincipal() instanceof String principalString) {
            username = principalString;
            roles = extractAuthoritiesFromUsername(username);
        } else {
            throw new IllegalArgumentException("Unsupported principal type: " +
                    authentication.getPrincipal().getClass());
        }

        return buildToken(username, roles);
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private String buildToken(String username, List<String> roles) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtExpirationMs)))
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    private List<String> extractAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private List<String> extractAuthoritiesFromUsername(String username) {
        return extractAuthorities(
                userDetailsService.loadUserByUsername(username).getAuthorities()
        );
    }
}
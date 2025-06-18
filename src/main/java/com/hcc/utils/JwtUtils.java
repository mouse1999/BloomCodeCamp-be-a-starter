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

//
//
//import com.hcc.entities.User;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.Date;
//import java.util.function.Function;
//
//@Component
//public class JWTUtils {
//
//    //how long is the token valid? a whole day
//    public static final long JWT_TOKEN_VALIDITY = 6000 * 60000 * 24;
//
//    // get the jwt secret from the properties file
//    @Value("${jwt.secret}")
//    private String secret;
//
//    //get username from token
//    public String getUsernameFromToken(String token){
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    //get the claims (not sure which datatype- make generic to pass the claim) from token-objects inside jwt
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver ){
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    private Claims getAllClaimsFromToken(String token){
//        return Jwts.parser()
//                .setSigningKey(secret)
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    //check if token is expired
//    public Date getExpirationDateFromToken(String token){
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    public boolean isTokenExpired(String token){
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }
//
//    //generate token
//    public String generateToken(User user){
//        return doGenerateToken(user.getUsername());
//
//    }
//
//    private String doGenerateToken(String subject){
//        Claims claims = Jwts.claims().setSubject(subject);
//        claims.put("scopes",
//                Arrays.asList(new SimpleGrantedAuthority("LEARNER_ROLE"),
//                new SimpleGrantedAuthority("CODE_REVIEWER_ROLE")));
//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
//                .signWith(SignatureAlgorithm.HS256, secret)
//                .compact();
//    }
//
//    //validate token
//
//    public boolean validateToken(String token, UserDetails userDetails){
//        final String username = getUsernameFromToken(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }
//
//}
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private final Key jwtSecret;

    private final int jwtExpirationMs;

    @Autowired
    private UserDetailsService userDetailsService;

    public JwtUtils(String jwtSecret, int jwtExpirationMs) {
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;

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
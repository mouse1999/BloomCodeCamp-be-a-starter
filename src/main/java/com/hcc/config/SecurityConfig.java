package com.hcc.config;

import com.hcc.exceptions.AuthExceptionHandler;
import com.hcc.exceptions.CustomAccessDeniedHandler;
import com.hcc.filters.JwtAuthenticationFilter;
import com.hcc.filters.JwtAuthorizationFilter;
import com.hcc.security.CustomAuthenticationManager;
import com.hcc.services.UserDetailsServiceImpl;
import com.hcc.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private  UserDetailsServiceImpl userDetailsService;

    @Autowired
    private  JwtUtils jwtUtils;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  AuthExceptionHandler authExceptionHandler;
    @Autowired
    private  CustomAccessDeniedHandler customAccessDeniedHandler;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(authenticationManager(), jwtUtils);

        jwtAuthFilter.setFilterProcessesUrl("/api/auth/login");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(CorsConfig.corsConfigurationSource()))


                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/login",
                                "/api/auth/register",
                                "/api/auth/validate",
                                "/auth/register",
                                "/static/**",
                                "/error"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .addFilter(jwtAuthFilter)
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtils, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class)


                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authExceptionHandler)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // Configure logout (JWT specific)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Logout successful\"}");
                        })
                        .deleteCookies("JWT") // Only JWT cookie for stateless
                        .permitAll()
                );

        // Remove formLogin if using pure JWT
        http.formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager() {
        return new CustomAuthenticationManager(userDetailsService, passwordEncoder);
    }



}

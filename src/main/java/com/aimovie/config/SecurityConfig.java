package com.aimovie.config;

import com.aimovie.entity.Role;
import com.aimovie.entity.User;
import com.aimovie.repository.UserRepository;
import com.aimovie.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                
                return new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        user.isEnabled(),
                        true, true, true,
                        getAuthorities(user.getRoles())
                );
            }
        };
    }

    private Collection<SimpleGrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/health", "/error").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/movies/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/movies/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                .requestMatchers("/api/actors/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()   
                .requestMatchers("/api/videos/**").permitAll() 
                .requestMatchers("/api/images/**").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/movies/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/api/streaming/**").permitAll() 
                .requestMatchers("/api/ratings/**").permitAll() 
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN", "MODERATOR", "UPLOADER")
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MODERATOR", "UPLOADER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(authService, userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allowed origins can be expanded via property if needed
        configuration.setAllowedOrigins(Arrays.asList(
                "https://www.phimnhalam.website",
                "https://phimnhalam.website",
                "https://cdn.phimnhalam.website",
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @RequiredArgsConstructor
    public static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final AuthService authService;
        private final UserRepository userRepository;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (authService.validateToken(token)) {
                    String username = authService.getUsernameFromToken(token);
                    Long userId = authService.getUserIdFromToken(token);
                    
                    // Load user and set authentication
                    User user = userRepository.findByUsername(username).orElse(null);
                    if (user != null && user.isEnabled()) {
                        Set<Role> roles = user.getRoles();
                        Collection<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(Collectors.toList());
                        
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // Add user info to request attributes for controllers to use
                        request.setAttribute("username", username);
                        request.setAttribute("userId", userId);
                    }
                }
            }
            
            filterChain.doFilter(request, response);
        }
    }
}
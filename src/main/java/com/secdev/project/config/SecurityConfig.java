package com.secdev.project.config;

import com.secdev.project.model.User;
import com.secdev.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final UserService userService;
    private final AntiBruteForce antiBruteForce;

    public SecurityConfig(@Lazy UserService userService, @Lazy AntiBruteForce antiBruteForce) {
        this.userService = userService;
        this.antiBruteForce = antiBruteForce;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userService.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Login failed."));

            userService.checkAndUnlockIfExpired(user);

            if (!user.isAccountNonLocked()) {
                throw new UsernameNotFoundException("Account is locked.");
            }

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.isEnabled(),
                    true,
                    true,
                    user.isAccountNonLocked(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .addFilterBefore(antiBruteForce, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/uploads/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    String email = authentication.getName();
                    String ip = getClientIp(request);
                    userService.recordLoginAttempt(email, true, ip);
                    response.sendRedirect("/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                    String email = request.getParameter("username");
                    String ip = getClientIp(request);
                    userService.recordLoginAttempt(email, false, ip);
                    
                    if (email != null && userService.shouldLockByEmail(email)) {
                        userService.lockAccount(email);
                    }
                    response.sendRedirect("/login?error");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }

    /**
     * Helper to get client IP. 
     * Handles cases where the app might be behind a proxy.
     */
    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf == null || xf.isBlank()) {
            return request.getRemoteAddr();
        }
        return xf.split(",")[0].trim();
    }
}
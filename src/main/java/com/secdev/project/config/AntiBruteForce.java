package com.secdev.project.config;

import com.secdev.project.service.UserService;
import com.secdev.project.service.exceptions.TooManyAttemptsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AntiBruteForce extends OncePerRequestFilter {

    private final UserService userService;

    public AntiBruteForce(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("/login".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String email = request.getParameter("username");
        String ip = getClientIp(request);

        try {
            userService.assertNotBlocked(email, ip); 
            filterChain.doFilter(request, response);
        } catch (TooManyAttemptsException ex) {
            response.sendRedirect("/login?blocked");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        return (xf == null || xf.isBlank()) ? request.getRemoteAddr() : xf.split(",")[0].trim();
    }
}

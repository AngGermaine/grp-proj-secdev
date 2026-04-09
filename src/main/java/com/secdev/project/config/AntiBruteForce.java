package com.secdev.project.config;

import com.secdev.project.service.UserService;
import com.secdev.project.service.exceptions.TooManyAttemptsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AntiBruteForce extends OncePerRequestFilter {

    private final UserService userService;
    @Value("${security.trust-proxy:false}")
    private boolean trustProxy;

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
        if (trustProxy) {
            String xf = request.getHeader("X-Forwarded-For");
            if (xf != null && !xf.isBlank()) {
                return xf.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}

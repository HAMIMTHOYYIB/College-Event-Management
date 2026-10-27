package com.example.basicweb.security;

import com.example.basicweb.entity.User;
import com.example.basicweb.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = resolveToken(request);

        if (token != null && jwtService.isTokenValid(token)) {
            String email = jwtService.extractEmail(token);
            if (email != null) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    request.setAttribute("currentUser", user);
                    request.setAttribute("authenticatedUser", user.getUsername());

                    boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
                    request.setAttribute("isAdmin", isAdmin);

                    // Role check: Only ADMIN can access /admin routes
                    String uri = request.getRequestURI();
                    if (uri != null && uri.startsWith("/admin") && !isAdmin) {
                        response.sendRedirect("/");
                        return false;
                    }

                    return true;
                }
            }
        }

        // Token is missing, invalid, or user was not found: clear stale cookie
        Cookie clearCookie = new Cookie("JWT_TOKEN", "");
        clearCookie.setHttpOnly(true);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);

        // Redirect unauthenticated user to login
        response.sendRedirect("/login");
        return false;
    }

    public String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}

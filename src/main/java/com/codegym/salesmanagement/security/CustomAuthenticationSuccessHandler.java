package com.codegym.salesmanagement.security;

import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isPresent()) {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            // Điều hướng dựa trên vai trò người dùng
            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
            } else if (roles.contains("ROLE_USER")) {
                response.sendRedirect("/");
            }
        } else {
            response.sendRedirect("/login?error=true");
        }
    }
}

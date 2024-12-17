package com.codegym.salesmanagement.security;

import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String username = oAuth2User.getAttribute("username");

        if (username == null) {
            response.sendRedirect("/login?error=true");
            return;
        }

        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

            // Thêm thông tin username vào Authentication token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user, null, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

            Boolean isNewUser = (Boolean) session.getAttribute("isNewUser");
            if (isNewUser != null && isNewUser) {
                session.removeAttribute("isNewUser");
                response.sendRedirect("/profile");
                return;
            }

            if (user.getRole() == User.Role.ADMIN) {
                response.sendRedirect("/admin/dashboard");
            } else {
                response.sendRedirect("/");
            }
        } else {
            response.sendRedirect("/login?error=true");
        }
    }
}


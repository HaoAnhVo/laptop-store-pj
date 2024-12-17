package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.concurrent.atomic.AtomicReference;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            Object principal = authentication.getPrincipal();
            String userLogged = null;
            AtomicReference<String> userAvatar = new AtomicReference<>();

            if (principal instanceof OAuth2User oAuth2User) {
                String username = oAuth2User.getName();
                if (username != null) {
                    userLogged = username.split("@")[0];
                    userService.findByUsername(username)
                            .ifPresent(user -> {
                                userAvatar.set(user.getAvatar());
                            });
                }
            } else if (principal instanceof UserDetails userDetails) {
                userLogged = userDetails.getUsername();
                userService.findByUsername(userLogged)
                        .ifPresent(user -> {
                            userAvatar.set(user.getAvatar());
                        });
            }

            if (userLogged != null) {
                model.addAttribute("userLogged", userLogged);
            }
            if (userAvatar.get() != null) {
                model.addAttribute("userAvatar", userAvatar);
            }
        }
    }
}


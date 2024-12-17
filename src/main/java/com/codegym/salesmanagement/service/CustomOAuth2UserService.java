package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Không thể lấy email từ thông tin xác thực của Google.");
        }

        // Tìm user trong DB, hoặc tạo mới nếu không tồn tại
        AtomicBoolean isNewUser = new AtomicBoolean(false);
        User user = userService.findByEmail(email).orElseGet(() -> {
            isNewUser.set(true);
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setFullname(fullName);
            newUser.setPhone("0000000000");
            newUser.setAddress("N/A");
            newUser.setPassword(userService.encodePassword("123456"));
            newUser.setRole(User.Role.USER);
            newUser.setEnabled(true);
            newUser.setLocked(false);
            return userService.save(newUser);
        });

        httpSession.setAttribute("isNewUser", isNewUser.get());

        String username = user.getUsername();
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("username", username);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new DefaultOAuth2User(authorities, attributes, "username");
    }
}

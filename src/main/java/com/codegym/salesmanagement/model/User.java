package com.codegym.salesmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Entity
@Table(name = "users")
@Data
public class User implements OAuth2User {
    private static final String DEFAULT_AVATAR = "/images/uploads/avatars/default-avatar.jpg";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Size(min = 6, message = "Mật khẩu tối thiểu phải có 6 ký tự")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Email(message = "Sai định dạng email")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    @Column(name = "full_name", nullable = false)
    private String fullname;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Sai định dạng số điện thoại")
    @Column(name = "phone", unique = true, nullable = false)
    private String phone;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Column(name = "address", nullable = false)
    private String address;

    @Getter(AccessLevel.NONE)
    private String avatar;

    public String getAvatar() {
        return avatar != null && !avatar.isEmpty() ? avatar : DEFAULT_AVATAR;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;


    public enum Role {
        ADMIN, USER
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getName() {
        return username;
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private VerificationToken verificationToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

}

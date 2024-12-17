package com.codegym.salesmanagement.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PasswordEncryptor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void encryptAndSavePasswords() {
        String sql = "SELECT user_id, password FROM users";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);

        for (Map<String, Object> user : users) {
            Long userId = (Long) user.get("user_id");
            String password = (String) user.get("password");

            // Kiểm tra xem mật khẩu đã mã hóa hay chưa
            if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                String encodedPassword = passwordEncoder.encode(password);
                String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
                jdbcTemplate.update(updateSql, encodedPassword, userId);
            }
        }
    }
}

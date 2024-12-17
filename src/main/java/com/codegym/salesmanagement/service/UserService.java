package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.PasswordResetToken;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.model.VerificationToken;
import com.codegym.salesmanagement.repository.IPasswordResetTokenRepository;
import com.codegym.salesmanagement.repository.IUserRepository;
import com.codegym.salesmanagement.repository.IVerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    @Autowired
    private IUserRepository iUserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

    @Autowired
    private IVerificationTokenRepository tokenRepository;

    @Autowired
    private IPasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private OrderService orderService;

    public boolean existsByUsername(String username) {
        return iUserRepository.findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return iUserRepository.findByEmail(email).isPresent();
    }

    public boolean existsByPhone(String phone) {
        return iUserRepository.findByPhone(phone).isPresent();
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }


    public Page<User> findUsersWithRoleAndSearchTerm(User.Role role, String searchTerm, Pageable pageable) {
        return iUserRepository.findByRoleAndSearchTerm(role, searchTerm, pageable);
    }

    public User getUserById(Long userId) {
        return iUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản này trên hệ thống!"));
    }

    public Optional<User> findByUsername(String username) {
        return iUserRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return iUserRepository.findByEmail(email);
    }

    public User createUser(User user) {
        User savedUser = iUserRepository.save(user);
        cartService.createCart(savedUser);
        return savedUser;
    }

    public void createUserWithRole(User user, String role) {
        user.setRole(User.Role.valueOf(role));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        iUserRepository.save(user);
        cartService.createCart(user);
    }

    public void enableUserByVerificationToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken != null) {
            User user = verificationToken.getUser();
            user.setEnabled(true);
            iUserRepository.save(user);
        }
    }

    public User save(User user) {
        return iUserRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = iUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        if (orderService.isUserHasOrders(userId)) {
            throw new IllegalStateException("Không thể xóa tài khoản vì người dùng đã có lịch sử giao dịch!");
        }

        passwordResetTokenRepository.deleteByUser(user);
        tokenRepository.deleteByUser(user);
        iUserRepository.delete(user);
    }

    public int findPageContainingUser(Long userId, int pageSize) {
        List<User> users = iUserRepository.findByRole(User.Role.USER);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(userId)) {
                return i / pageSize;
            }
        }
        return 0;
    }

    public long countUsersWithRole(User.Role role) {
        return iUserRepository.countByRole(role);
    }


    // Verify token
    public String createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);
        return token;
    }

    public boolean verifyToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null || verificationToken.isExpired()) {
            return false;
        }
        User user = verificationToken.getUser();
        if (!user.isEnabled()) {
            user.setEnabled(true);
            iUserRepository.save(user);
        }
        tokenRepository.delete(verificationToken);
        return true;
    }

    // reset Token
    public String generateResetToken(String email) {
        Optional<User> userOpt = iUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email không tồn tại trong hệ thống!");
        }

        User user = userOpt.get();

        // Xóa token cũ nếu có
        passwordResetTokenRepository.deleteByUser(user);
        // Tạo token mới
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
        PasswordResetToken resetToken = new PasswordResetToken(token, expiryDate, user);
        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    public void resetPasswordWithToken(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn!");
        }

        // Cập nhật mật khẩu
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        iUserRepository.save(user);

        // Xóa token
        passwordResetTokenRepository.delete(resetToken);
    }

    // kiểm tra mật khẩu cũ
    public boolean checkPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public void changePassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
    }

}

package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.model.dto.UserProfileDTO;
import com.codegym.salesmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;


@Controller
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private UserService userService;

    @Value("${upload.path:D:/uploaded-files/avatars}")
    private String uploadPath;

    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));

        UserProfileDTO userProfileDTO = mapUserToUserProfileDTO(currentUser);
        model.addAttribute("userProfile", userProfileDTO);
        return "personal/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userProfile") UserProfileDTO userProfileDTO,
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));

        if (result.hasErrors()) {
            return "personal/profile";
        }

        if (isEmailOrPhoneDuplicated(userProfileDTO, currentUser, result)) {
            return "personal/profile";
        }

        updateUserInfo(userProfileDTO, currentUser);
        userService.save(currentUser);

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/profile";
    }

    @GetMapping("/changePassword")
    public String showChangePasswordForm() {
        return "personal/changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));
        if (userService.checkPassword(user, oldPassword)) {
            userService.changePassword(user, newPassword);
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Thay đổi mật khẩu thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không đúng! Vui lòng thử lại");
        }
        return "redirect:/profile/changePassword";
    }


    private UserProfileDTO mapUserToUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setFullname(user.getFullname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatarPath(user.getAvatar());
        return dto;
    }

    private boolean isEmailOrPhoneDuplicated(UserProfileDTO dto, User currentUser, BindingResult result) {
        if (userService.existsByEmail(dto.getEmail()) && !dto.getEmail().equals(currentUser.getEmail())) {
            result.rejectValue("email", "error.user", "Email này đã tồn tại!");
            return true;
        }

        if (userService.existsByPhone(dto.getPhone()) && !dto.getPhone().equals(currentUser.getPhone())) {
            result.rejectValue("phone", "error.user", "Số điện thoại này đã tồn tại!");
            return true;
        }
        return false;
    }

    private void updateUserInfo(UserProfileDTO dto, User user) {
        user.setFullname(dto.getFullname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
    }

    @PostMapping("/update-avatar")
    @ResponseBody
    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile avatar, Principal principal) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));

        String error = processAvatarUploadForAjax(avatar, currentUser);
        if (error != null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", error));
        }

        userService.save(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newAvatarPath", currentUser.getAvatar());

        return ResponseEntity.ok(response);
    }

    private String processAvatarUploadForAjax(MultipartFile avatarFile, User currentUser) {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String mimeType = avatarFile.getContentType();
            if (mimeType == null || !mimeType.startsWith("image/")) {
                return "File upload không phải định dạng ảnh!";
            }

            String uploadDir = uploadPath;
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            deleteOldAvatar(uploadDir, currentUser);

            String fileName = generateUniqueFileName(currentUser);
            File destFile = new File(uploadDir, fileName);

            try {
                avatarFile.transferTo(destFile);
                currentUser.setAvatar("/images/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return "Lỗi khi upload file!";
            }
        }
        return null;
    }

    private String generateUniqueFileName(User user) {
        return "user_" + user.getId() + "_" + UUID.randomUUID() + ".png";
    }

    private void deleteOldAvatar(String uploadDir, User currentUser) {
        if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
            String oldAvatarPath = uploadDir + File.separator +
                    currentUser.getAvatar().replace("/images/uploads/", "");
            File oldAvatarFile = new File(oldAvatarPath);

            if (oldAvatarFile.exists() && !oldAvatarFile.getName().equals("default-avatar.jpg")) {
                oldAvatarFile.delete();
            }
        }
    }

}

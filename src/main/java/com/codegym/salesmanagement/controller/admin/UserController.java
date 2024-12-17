package com.codegym.salesmanagement.controller.admin;

import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.findUsersWithRoleAndSearchTerm(User.Role.USER, keyword, pageable);

        int totalPages = userPage.getTotalPages();
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPage", page);

        if (model.containsAttribute("message")) {
            model.addAttribute("toastMessage", model.getAttribute("message"));
        }
        return "admin/user/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user/form";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user/form";
        }

        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Tên tài khoản đã tồn tại!");
            return "admin/user/form";
        }

        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email này đã tồn tại!");
            return "admin/user/form";
        }

        if (userService.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "Số điện thoại này đã tồn tại!");
            return "admin/user/form";
        }

        userService.createUserWithRole(user, user.getRole().name());
        user.setEnabled(true);

        long totalUsers = userService.countUsersWithRole(User.Role.USER);
        int lastPage = (int) Math.ceil((double) totalUsers / 5) - 1;
        redirectAttributes.addFlashAttribute("message", "Tài khoản người dùng mới đã được tạo thành công!");
        return "redirect:/admin/users?page=" + lastPage;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user/form";
    }

    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes) {
        User existingUser = userService.getUserById(user.getId());

        if (!existingUser.getEmail().equals(user.getEmail()) && userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email này đã tồn tại!");
            return "admin/user/form";
        }

        if (!existingUser.getPhone().equals(user.getPhone()) && userService.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "Số điện thoại này đã tồn tại!");
            return "admin/user/form";
        }


        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(userService.encodePassword(user.getPassword()));
        }

        existingUser.setFullname(user.getFullname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setAddress(user.getAddress());

        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        userService.save(existingUser);

        int pageOfUser = userService.findPageContainingUser(user.getId(), 5);
        redirectAttributes.addFlashAttribute("message", "Cập nhật tài khoản thành công!");
        return "redirect:/admin/users?page=" + pageOfUser;
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            long totalUsers = userService.countUsersWithRole(User.Role.USER);
            int totalPages = (int) Math.ceil((double) totalUsers / 5);

            if (page >= totalPages && totalPages > 0) {
                page = totalPages - 1;
            }

            redirectAttributes.addFlashAttribute("message", "Tài khoản đã bị xóa thành công!");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/admin/users?page=" + page;
    }

    @GetMapping("/lock/{id}")
    public String lockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        user.setLocked(true);
        userService.save(user);
        int pageOfUser = userService.findPageContainingUser(user.getId(), 5);
        redirectAttributes.addFlashAttribute("message", "Tài khoản đã bị khóa!");
        return "redirect:/admin/users?page=" + pageOfUser;
    }

    @GetMapping("/unlock/{id}")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        user.setLocked(false);
        userService.save(user);
        int pageOfUser = userService.findPageContainingUser(user.getId(), 5);
        redirectAttributes.addFlashAttribute("message", "Tài khoản đã được mở khóa!");
        return "redirect:/admin/users?page=" + pageOfUser;
    }

}

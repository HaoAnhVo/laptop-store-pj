package com.codegym.salesmanagement.controller.admin;

import com.codegym.salesmanagement.model.Post;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.PostService;
import com.codegym.salesmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Value("${upload.path:D:/uploaded-files/post-images}")
    private String uploadPathPostImage;

    @GetMapping
    public String listPosts(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postService.searchPosts(keyword, pageable);

        int totalPages = postPage.getTotalPages();
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("postPage", postPage);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("keyword", keyword);
        return "admin/post/list";
    }

    @GetMapping("/detail/{id}")
    public String viewPostAdmin(@PathVariable Long id, Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        model.addAttribute("backUrl", "/admin/posts");
        return "admin/post/view";
    }

    @GetMapping("/create/{id}")
    public String showCreateForm(HttpServletRequest request, Model model, @PathVariable String id) {
        model.addAttribute("contextPath", request.getContextPath());
        model.addAttribute("post", new Post());
        return "admin/post/form";
    }

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute("post") Post post, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/post/form";
        }

        postService.save(post);
        long totalPosts = postService.countPost();
        int lastPage = (int) Math.ceil((double) totalPosts / 5) - 1;
        redirectAttributes.addFlashAttribute("message", "Bài viết mới đã được tạo thành công!");
        return "redirect:/admin/posts?page=" + lastPage;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, HttpServletRequest request, Model model) {
        model.addAttribute("contextPath", request.getContextPath());
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "admin/post/form";
    }

    @PostMapping("/update")
    public String updatePost(@Valid @ModelAttribute("post") Post post, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/post/form";
        }

        postService.save(post);
        int pageOfPost = postService.findPageContainingPost(post.getId(), 5);
        redirectAttributes.addFlashAttribute("message", "Cập nhật bài viết thành công!");

        return "redirect:/admin/posts?page=" + pageOfPost;
    }

    @PostMapping("/upload-image")
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = uploadPathPostImage;
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath);

            Map<String, String> response = new HashMap<>();
            response.put("location", "/images/uploads/" + filename);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh", e);
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             RedirectAttributes redirectAttributes) {
        try {
            postService.deleteById(id);
            long totalPosts = postService.countPost();
            int totalPages = (int) Math.ceil((double) totalPosts / 5);

            if (page >= totalPages && totalPages > 0) {
                page = totalPages - 1;
            }

            redirectAttributes.addFlashAttribute("message", "Bài viết đã bị xóa thành công!");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/admin/posts?page=" + page;
    }
}

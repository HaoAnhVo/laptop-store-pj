package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.model.Comment;
import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.Review;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.CommentService;
import com.codegym.salesmanagement.service.ProductService;
import com.codegym.salesmanagement.service.ReviewService;
import com.codegym.salesmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/review")
public class ReviewController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public String addReview(@RequestParam Long productId,
                            @ModelAttribute("review") Review review,
                            Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đánh giá sản phẩm.");
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));
        Product product = productService.getProductById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
            return "redirect:/products";
        }

        review.setUser(user);
        review.setProduct(product);
        reviewService.saveReview(review);

        redirectAttributes.addFlashAttribute("message", "Đánh giá đã được thêm thành công!");
        return "redirect:/products/details?productId=" + productId;
    }

    @PostMapping("/edit")
    public String editReview(@RequestParam Long productId,
                             @ModelAttribute("review") Review updatedReview,
                             Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để chỉnh sửa đánh giá.");
            return "redirect:/products/details?productId=" + updatedReview.getProduct().getId();
        }

        Review existingReview = reviewService.getReviewById(updatedReview.getId());
        if (existingReview == null || !existingReview.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Không thể chỉnh sửa đánh giá không thuộc về bạn!");
            return "redirect:/products/details?productId=" + updatedReview.getProduct().getId();
        }

        existingReview.setContent(updatedReview.getContent());
        existingReview.setRating(updatedReview.getRating());
        reviewService.saveReview(existingReview);

        redirectAttributes.addFlashAttribute("message", "Đánh giá đã được chỉnh sửa thành công!");
        return "redirect:/products/details?productId=" + updatedReview.getProduct().getId();
    }

    @PostMapping("/delete")
    public String deleteReview(@RequestParam Long reviewId, @RequestParam Long productId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xóa đánh giá.");
            return "redirect:/products/details?productId=" + productId;
        }

        Review review = reviewService.getReviewById(reviewId);
        if (review == null || !review.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa đánh giá không thuộc về bạn!");
            return "redirect:/products/details?productId=" + productId;
        }

        reviewService.deleteReview(reviewId);
        redirectAttributes.addFlashAttribute("message", "Đánh giá đã được xóa thành công!");
        return "redirect:/products/details?productId=" + productId;
    }

    @PostMapping("/{reviewId}/comments/add")
    public String addComment(@PathVariable Long reviewId,
                             @RequestParam("content") String content,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        Review review = reviewService.getReviewById(reviewId);
        if (review == null) {
            redirectAttributes.addFlashAttribute("error", "Đánh giá không tồn tại.");
            return "redirect:/admin/product/detail";
        }
        Comment comment = new Comment();
        comment.setReview(review);
        comment.setContent(content);
        comment.setAuthor(principal.getName());
        commentService.saveComment(comment);

        redirectAttributes.addFlashAttribute("message", "Bình luận đã được thêm thành công!");
        return "redirect:/admin/products/detail/" + reviewId;
    }

    @GetMapping("/comments/edit/{commentId}")
    @ResponseBody
    public String getEditCommentForm(@PathVariable Long commentId, Principal principal) {
        Comment comment = commentService.getCommentById(commentId);
        if (comment == null || !comment.getAuthor().equals(principal.getName())) {
            return "<p>Không thể chỉnh sửa bình luận này!</p>";
        }
        return "<form class='edit-comment-form' action='/products/details/review/comments/update' method='post'>" +
                "    <input type='hidden' name='commentId' value='" + comment.getId() + "'/>" +
                "    <textarea name='content' rows='2'>" + comment.getContent() + "</textarea>" +
                "    <button type='submit'>Cập nhật</button>" +
                "</form>";
    }

    @PostMapping("/comments/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateComment(@RequestParam Long commentId,
                                                             @RequestParam String content,
                                                             Principal principal) {
        Comment comment = commentService.getCommentById(commentId);
        if (comment == null || !comment.getAuthor().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Không thể cập nhật bình luận này!"));
        }

        comment.setContent(content);
        commentService.saveComment(comment);

        Long productId = comment.getReview().getProduct().getId();

        return ResponseEntity.ok(Map.of("success", true, "updatedContent", content, "productId", productId));
    }


    @GetMapping("/comments/delete/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long commentId, Principal principal) {
        Comment comment = commentService.getCommentById(commentId);
        if (comment == null || !comment.getAuthor().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Không thể xóa bình luận này!"));
        }
        commentService.deleteComment(commentId);
        Long productId = comment.getReview().getProduct().getId();
        return ResponseEntity.ok(Map.of("success", true, "message", "Bình luận đã được xóa!", "productId", productId));
    }
}

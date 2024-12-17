package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.*;
import com.codegym.salesmanagement.repository.IUserRepository;
import com.codegym.salesmanagement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("")
public class HomeController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String showHomePage(@RequestParam(value = "productType", required = false) Long productTypeId,
                               @RequestParam(value = "minPrice", required = false) Double minPrice,
                               @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                               @RequestParam(value = "productName", required = false, defaultValue = "") String productName,
                               Model model) {
        boolean isSearch = productTypeId != null || minPrice != null || maxPrice != null || (productName != null && !productName.trim().isEmpty());

        List<Product> products = productService.filterAndSearchProductsForHome(productTypeId, minPrice, maxPrice, productName);
        List<ProductType> productTypes = productService.getAllProductTypes();

        List<Product> topSellingProducts = productService.getTopSellingProducts(4);
        for (Product product : topSellingProducts) {
            product.setFormattedPrice(PriceFormatter.formatPrice(product.getPrice()));
        }

        Map<Long, List<Product>> productsByType = products.stream()
                .collect(Collectors.groupingBy(p -> p.getProductType().getId()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().limit(4).toList()));

        List<ProductType> filteredProductTypes = productTypes.stream()
                .filter(type -> productsByType.containsKey(type.getId()))
                .toList();

        for (Product product : products) {
            product.setFormattedPrice(PriceFormatter.formatPrice(product.getPrice()));
        }

        model.addAttribute("productsByType", productsByType);
        model.addAttribute("filteredProductTypes", filteredProductTypes);
        model.addAttribute("productTypes", productTypes);
        model.addAttribute("products", products);
        model.addAttribute("topSellingProducts", topSellingProducts);
        model.addAttribute("productTypeId", productTypeId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("productName", productName);
        model.addAttribute("isSearch", isSearch);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                String username = userDetails.getUsername();

                List<Notification> notifications = notificationService.getUnreadNotifications(username);
                Long unreadCount = notificationService.getUnreadNotificationCount(username);

                model.addAttribute("notifications", notifications);
                model.addAttribute("unreadNotifications", unreadCount);
            }
        }
        return "home";
    }

    @GetMapping("/products")
    public String listProductsByType(
            @RequestParam(value = "productTypeId", required = false) Long productTypeId,
            Model model) {
        if (productTypeId == null) {
            model.addAttribute("error", "Vui lòng chọn một loại sản phẩm hợp lệ.");
            return "error";
        }
        ProductType productType = productService.getProductTypeById(productTypeId);
        if (productType == null) {
            model.addAttribute("error", "Không tìm thấy loại sản phẩm với ID: " + productTypeId);
            return "error";
        }
        List<Product> products = productService.getProductsByType(productTypeId);

        for (Product product : products) {
            product.setFormattedPrice(PriceFormatter.formatPrice(product.getPrice()));
        }

        model.addAttribute("products", products);
        model.addAttribute("productType", productType);
        model.addAttribute("productTypeId", productTypeId);
        return "product/list";
    }

    @GetMapping("/products/details")
    public String showProductDetails(@RequestParam("productId") Long productId, Model model, Principal principal) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            model.addAttribute("error", "Không tìm thấy sản phẩm với ID: " + productId);
            return "error";
        }
        product.setFormattedPrice(PriceFormatter.formatPrice(product.getPrice()));

        ProductType productType = product.getProductType();
        List<Review> reviews = reviewService.getReviewsByProductId(productId);

        long count5Stars = reviews.stream().filter(r -> r.getRating() == 5).count();
        long count4Stars = reviews.stream().filter(r -> r.getRating() == 4).count();
        long count3Stars = reviews.stream().filter(r -> r.getRating() == 3).count();
        long count2Stars = reviews.stream().filter(r -> r.getRating() == 2).count();
        long count1Star = reviews.stream().filter(r -> r.getRating() == 1).count();

        double averageRating = reviewService.calculateAverageRating(productId);
        model.addAttribute("product", product);
        model.addAttribute("productType", productType);
        model.addAttribute("reviews", reviews);
        model.addAttribute("count5Stars", count5Stars);
        model.addAttribute("count4Stars", count4Stars);
        model.addAttribute("count3Stars", count3Stars);
        model.addAttribute("count2Stars", count2Stars);
        model.addAttribute("count1Star", count1Star);
        model.addAttribute("averageRating", averageRating);

        if (principal != null) {
            String username = principal.getName();
            userService.findByUsername(username).ifPresent(user -> model.addAttribute("currentUser", user));
        }
        return "product/details";
    }

    @GetMapping("/posts")
    public String viewAllPosts(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/posts/detail/{id}")
    public String viewPostUser(@PathVariable Long id, Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        model.addAttribute("backUrl", "/posts");
        return "admin/post/view";
    }

    @GetMapping("/apple-products")
    public String listAppleProducts(Model model) {
        String typeName = "Apple";
        List<Product> appleProducts = productService.getProductsByTypeName(typeName);

        for (Product product : appleProducts) {
            product.setFormattedPrice(PriceFormatter.formatPrice(product.getPrice()));
        }

        model.addAttribute("products", appleProducts);
        model.addAttribute("productTypeName", typeName);
        return "product/apple-products";
    }
}

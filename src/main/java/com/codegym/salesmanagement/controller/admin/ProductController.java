package com.codegym.salesmanagement.controller.admin;

import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.Review;
import com.codegym.salesmanagement.service.ProductService;
import com.codegym.salesmanagement.service.ReviewService;
import com.codegym.salesmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "productType", required = false) Long productTypeId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "productName", required = false, defaultValue = "") String productName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        boolean isFilterApplied = (productTypeId != null && productTypeId != 0L) ||
                minPrice != null || maxPrice != null ||
                (productName != null && !productName.trim().isEmpty());

        Page<Product> productPage;
        if (isFilterApplied) {
            productPage = productService.filterAndSearchProducts(productTypeId, minPrice, maxPrice, productName, 0, Integer.MAX_VALUE);
        } else {
            productPage = productService.filterAndSearchProducts(productTypeId, minPrice, maxPrice, productName, page, size);
        }

        for (Product product : productPage.getContent()) {
            product.setFormattedPrice(PriceFormatter.formatPrice(product.getPrice()));
        }

        int totalPages = productPage.getTotalPages();

        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("productPage", productPage);
        model.addAttribute("productTypeId", productTypeId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("productName", productName);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productTypes", productService.getAllProductTypes());
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPage", page);

        return "admin/product/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("productTypes", productService.getAllProductTypes());
        return "admin/product/form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product, RedirectAttributes redirectAttributes) {
        productService.saveProduct(product);

        long totalProducts = productService.countTotalProducts();
        int lastPage = (int) Math.ceil((double) totalProducts / 10) - 1;
        redirectAttributes.addFlashAttribute("message", "Sản phẩm đã được lưu thành công!");
        return "redirect:/admin/products?page=" + lastPage;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, @RequestParam(value = "page", defaultValue = "0") int page,Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("productTypes", productService.getAllProductTypes());
        model.addAttribute("currentPage", page);
        return "admin/product/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, @RequestParam(value = "page", defaultValue = "0") int page,RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Sản phẩm đã bị xóa!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        long totalProducts = productService.countTotalProducts();
        int totalPages = (int) Math.ceil((double) totalProducts / 10);

        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
        }
        return "redirect:/admin/products?page=" + page;
    }

    @GetMapping("/detail/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model, Principal principal) {
        Product product = productService.getProductById(id);
        product.setFormattedPrice(PriceFormatter.formatPrice(product.getPrice()));
        List<Review> reviews = reviewService.getReviewsByProductId(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);

        if (principal != null) {
            String username = principal.getName();
            userService.findByUsername(username).ifPresent(user -> model.addAttribute("currentUser", user));
        }
        return "admin/product/detail";
    }
}

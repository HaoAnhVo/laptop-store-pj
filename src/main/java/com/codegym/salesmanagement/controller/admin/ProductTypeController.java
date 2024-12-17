package com.codegym.salesmanagement.controller.admin;

import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.ProductType;
import com.codegym.salesmanagement.service.ProductService;
import com.codegym.salesmanagement.service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/productTypes")
public class ProductTypeController {
    @Autowired
    private ProductTypeService productTypeService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String listProductTypes(
            @RequestParam(value = "productTypeName", required = false) String productTypeName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductType> productTypePage = productTypeService.getPaginatedAndSearchedProductTypes(productTypeName, pageable);

        int totalPages = productTypePage.getTotalPages();

        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("productTypePage", productTypePage);
        model.addAttribute("productTypes", productTypePage.getContent());
        model.addAttribute("productTypeName", productTypeName);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPage", page);
        return "admin/productType/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("productType", new ProductType());
        return "admin/productType/form";
    }

    @PostMapping("/save")
    public String saveOrUpdateProductType(@ModelAttribute("productType") ProductType productType,
                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                          RedirectAttributes redirect) {
        if (productType.getId() != null) {
            // thao tác cập nhật
            ProductType existingProductType = productTypeService.getProductTypeById(productType.getId());
            if (existingProductType != null) {
                existingProductType.setTypeName(productType.getTypeName());

                productTypeService.saveProductType(existingProductType);
                redirect.addFlashAttribute("message", "Cập nhật nhãn hàng thành công!");
            } else {
                redirect.addFlashAttribute("errorMessage", "Không tìm thấy nhãn hàng để cập nhật!");
            }
        } else {
            // thao tác thêm
            productTypeService.saveProductType(productType);
            redirect.addFlashAttribute("successMessage", "Thêm mới nhãn hàng thành công!");
        }
        long totalProductTypes = productTypeService.countTotalProductTypes();
        int totalPages = (int) Math.ceil((double) totalProductTypes / 10);
        return "redirect:/admin/productTypes?page=" + (totalPages - 1);
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ProductType productType = productTypeService.getProductTypeById(id);
        model.addAttribute("productType", productType);
        return "admin/productType/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteProductType(@PathVariable Long id,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    RedirectAttributes redirect) {
        ProductType productType = productTypeService.getProductTypeById(id);
        if (productType != null) {
            if (!productType.getProducts().isEmpty()) {
                redirect.addFlashAttribute("message",
                        "Không thể xóa vì vẫn còn sản phẩm thuộc nhãn hàng này!");
                return "redirect:/admin/productTypes?page=" + page;
            }

            productTypeService.deleteProductTypeById(id);
            redirect.addFlashAttribute("message", "Xóa nhãn hàng thành công!");
        } else {
            redirect.addFlashAttribute("message", "Không tìm thấy nhãn hàng để xóa!");
        }

        long totalProductTypes = productTypeService.countTotalProductTypes();
        int totalPages = (int) Math.ceil((double) totalProductTypes / 10);

        if (totalPages > 0 && page >= totalPages) {
            page = totalPages - 1;
        }
        return "redirect:/admin/productTypes?page=" + page;
    }
}

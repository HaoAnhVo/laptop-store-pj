package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.ProductType;
import com.codegym.salesmanagement.repository.IProductTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductTypeService {
    @Autowired
    private IProductTypeRepository iProductTypeRepository;

    public ProductType getProductTypeById(Long id) {
        return iProductTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhãn hàng"));
    }

    public ProductType saveProductType(ProductType productType) {
        return iProductTypeRepository.save(productType);
    }

    public long countTotalProductTypes() {
        return iProductTypeRepository.count();
    }

    public void deleteProductTypeById(Long id) {
        iProductTypeRepository.deleteById(id);
    }

    public Page<ProductType> getPaginatedAndSearchedProductTypes(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isEmpty()) {
            return iProductTypeRepository.findAll(pageable);
        }
        return iProductTypeRepository.findByTypeNameContaining(keyword, pageable);
    }
}

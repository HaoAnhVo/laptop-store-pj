package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.ProductType;
import com.codegym.salesmanagement.repository.IOrderItemRepository;
import com.codegym.salesmanagement.repository.IProductRepository;
import com.codegym.salesmanagement.repository.IProductTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private IProductRepository iProductRepository;

    @Autowired
    private IProductTypeRepository iProductTypeRepository;

    @Autowired
    private IOrderItemRepository iOrderItemRepository;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private OrderService orderService;

    @PersistenceContext
    private EntityManager em;

    public ProductType getProductTypeById(Long productTypeId) {
        return iProductTypeRepository.findById(productTypeId).orElse(null);
    }

    public List<Product> getProductsByType(Long productTypeId) {
        if (productTypeId == null || productTypeId == 0L) {
            return iProductRepository.findAll();
        }
        return iProductRepository.findByProductTypeId(productTypeId);
    }

    public List<Product> getProductsByTypeName(String typeName) {
        ProductType productType = iProductTypeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại sản phẩm!"));
        return iProductRepository.findByProductTypeId(productType.getId());
    }


    public Product getProductById(Long id) {
        return iProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
    }

    public Product saveProduct(Product product) {
        return iProductRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (orderService.isProductInAnyOrder(id)) {
            throw new RuntimeException("Không thể xóa sản phẩm vì đã có đơn hàng liên quan.");
        }

        if (cartItemService.isProductInAnyCart(id)) {
            throw new RuntimeException("Không thể xóa sản phẩm vì đang nằm trong giỏ hàng của người dùng.");
        }

        iProductRepository.deleteById(id);
    }

    public long countTotalProducts() {
        return iProductRepository.count();
    }

    public long countOutOfStockProducts() {
        return iProductRepository.countByStock(0L);
    }

    public List<Product> getTopSellingProducts(int limit) {
        List<Object[]> results = iOrderItemRepository.findTopSellingProductsWithQuantity(PageRequest.of(0, limit));
        List<Product> products = new ArrayList<>();

        for (Object[] result : results) {
            Product product = (Product) result[0];
            Long soldQuantity = (Long) result[1];
            product.setSoldQuantity(soldQuantity);
            products.add(product);
        }

        return products;
    }

    public List<Product> getLowStockProducts(Long threshold) {
        return iProductRepository.findByStockLessThanEqual(threshold);
    }

    public List<ProductType> getAllProductTypes() {
        return iProductTypeRepository.findAll();
    }

    public Page<Product> filterAndSearchProducts(Long productTypeId, BigDecimal minPrice, BigDecimal maxPrice, String productName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        ProductType productType = null;

        if (productTypeId != null && productTypeId != 0L) {
            productType = iProductTypeRepository.findById(productTypeId).orElse(null);
        }
        return iProductRepository.findByFilters(productType, minPrice, maxPrice, productName, pageable);
    }

    @Transactional
    public List<Product> filterAndSearchProductsForHome(Long productTypeId, Double minPrice, Double maxPrice, String productName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);
        List<Predicate> predicates = new ArrayList<>();

        if (productTypeId != null && productTypeId != 0) {
            predicates.add(cb.equal(product.get("productType").get("id"), productTypeId));
        }
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(product.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(product.get("price"), maxPrice));
        }

        if (productName != null && !productName.isEmpty()) {
            predicates.add(cb.like(cb.lower(product.get("productName")), "%" + productName.toLowerCase() + "%"));
        }
        cq.select(product).distinct(true).where(predicates.toArray(new Predicate[0]));

        TypedQuery<Product> query = em.createQuery(cq);
        return query.getResultList();
    }

}

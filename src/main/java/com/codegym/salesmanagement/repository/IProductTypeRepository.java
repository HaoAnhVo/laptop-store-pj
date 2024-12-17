package com.codegym.salesmanagement.repository;

import com.codegym.salesmanagement.model.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductTypeRepository extends JpaRepository<ProductType, Long> {
    Page<ProductType> findByTypeNameContaining(String typeName, Pageable pageable);
    Optional<ProductType> findByTypeName(String typeName);
    long count();
}

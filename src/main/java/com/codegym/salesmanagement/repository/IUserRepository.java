package com.codegym.salesmanagement.repository;

import com.codegym.salesmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.fullname) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findByRoleAndSearchTerm(User.Role role, String searchTerm, Pageable pageable);

    long countByRole(User.Role role);
}

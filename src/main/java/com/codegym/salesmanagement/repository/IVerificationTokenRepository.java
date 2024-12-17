package com.codegym.salesmanagement.repository;

import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IVerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
    void deleteByUser(User user);
}

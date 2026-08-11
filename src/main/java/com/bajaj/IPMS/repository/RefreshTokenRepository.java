package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.RefreshToken;
import com.bajaj.IPMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByToken(String token);

    RefreshToken findByUser(User user);
}

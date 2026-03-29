package me.psikuvit.shecare.repository;

import me.psikuvit.shecare.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUserIdAndTokenHash(String userId, String tokenHash);
    void deleteByUserId(String userId);
}


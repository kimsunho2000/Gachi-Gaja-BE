// repository.com.gachi_gaja.server.UserRepository
package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByEmailAndUserIdNot(String email, UUID userId);
    boolean existsByNicknameAndUserIdNot(String nickname, UUID userId);
}

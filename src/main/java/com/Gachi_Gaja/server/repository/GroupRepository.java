package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.Group;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    static final String LOCK_TIMEOUT = "3000";

    List<Group> findByRegion(String region);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT)}) // 비관적락 3초 대기
    @Query("SELECT g FROM Group g WHERE g.groupId = :groupId")
    Optional<Group> findByIdWithPessimisticLock(UUID groupId);
}
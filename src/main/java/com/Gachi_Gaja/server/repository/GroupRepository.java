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

    /**
     * 이 값은 MySQL 에서 실제 대기 시간을 바꾸지 못한다.
     * Hibernate 의 MySQL 다이얼렉트는 jakarta.persistence.lock.timeout 힌트에서
     * 0(NOWAIT)과 -2(SKIP LOCKED)만 SQL 로 옮기고, 그 외의 값은 대응하는 문법이 없어 버린다.
     * 실제 대기 한도는 커넥션 세션 변수로 준다.
     *   spring.datasource.hikari.connection-init-sql=SET SESSION innodb_lock_wait_timeout = 3
     * 힌트는 잠금 의도를 코드에 남겨두기 위해 유지한다.
     */
    static final String LOCK_TIMEOUT = "3000";

    List<Group> findByRegion(String region);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT)})
    @Query("SELECT g FROM Group g WHERE g.groupId = :groupId")
    Optional<Group> findByIdWithPessimisticLock(UUID groupId);
}
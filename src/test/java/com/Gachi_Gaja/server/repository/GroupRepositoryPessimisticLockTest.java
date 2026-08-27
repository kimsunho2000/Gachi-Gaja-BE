package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GroupRepository pessimistic lock test.
 * Uses H2 in-memory DB with MODE=MySQL.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("GroupRepository 비관적 락 테스트")
class GroupRepositoryPessimisticLockTest {

    @Autowired
    private GroupRepository groupRepository;

    private Group savedGroup;

    @BeforeEach
    void setUp() {
        savedGroup = groupRepository.save(Group.builder()
                .title("테스트 그룹")
                .region("서울")
                .startingPoint("강남역")
                .endingPoint("홍대입구역")
                .transportation("지하철")
                .period("1박 2일")
                .budget(100000)
                .startingDay(LocalDate.now().plusDays(10))
                .endingDay(LocalDate.now().plusDays(11))
                .requirementDeadline(LocalDate.now().plusDays(5))
                .voteDeadline(null)
                .callCnt(3)
                .build());
    }

    @Test
    @DisplayName("[기본] findByIdWithPessimisticLock은 존재하는 그룹을 반환한다")
    void findByIdWithPessimisticLock_returnsGroup() {
        Optional<Group> result = groupRepository.findByIdWithPessimisticLock(savedGroup.getGroupId());

        assertThat(result).isPresent();
        assertThat(result.get().getGroupId()).isEqualTo(savedGroup.getGroupId());
        assertThat(result.get().getTitle()).isEqualTo("테스트 그룹");
    }

    @Test
    @DisplayName("[기본] 존재하지 않는 groupId로 조회하면 Optional.empty를 반환한다")
    void findByIdWithPessimisticLock_returnsEmpty_whenNotFound() {
        Optional<Group> result = groupRepository.findByIdWithPessimisticLock(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("[순차 검증] callCnt를 순차적으로 3번 감소시키면 최종값이 0이다")
    void pessimisticLock_sequentialCallCntDecrement() {
        UUID groupId = savedGroup.getGroupId();

        // 3번 순차 감소 (비관적 락이 보장하는 결과를 시뮬레이션)
        for (int i = 0; i < 3; i++) {
            groupRepository.findByIdWithPessimisticLock(groupId)
                    .ifPresent(Group::decreaseCallCnt);
        }

        Group updated = groupRepository.findById(groupId).orElseThrow();
        assertThat(updated.getCallCnt()).isEqualTo(0);
    }

    @Test
    @DisplayName("[순차 검증] 비관적 락 조회 후 그룹 데이터 수정이 정상 반영된다")
    void pessimisticLock_modificationPersists() {
        UUID groupId = savedGroup.getGroupId();

        Group locked = groupRepository.findByIdWithPessimisticLock(groupId).orElseThrow();
        locked.decreaseCallCnt();
        groupRepository.flush();

        Group reloaded = groupRepository.findById(groupId).orElseThrow();
        assertThat(reloaded.getCallCnt()).isEqualTo(2); // 3 - 1 = 2
    }

    @Test
    @DisplayName("[설정] findByIdWithPessimisticLock 메서드에 @Lock, @QueryHints가 선언되어 있다")
    void pessimisticLock_methodAnnotationsArePresent() throws NoSuchMethodException {
        var method = GroupRepository.class.getMethod(
                "findByIdWithPessimisticLock", UUID.class);

        var lockAnnotation = method.getAnnotation(
                org.springframework.data.jpa.repository.Lock.class);
        var queryHintsAnnotation = method.getAnnotation(
                org.springframework.data.jpa.repository.QueryHints.class);

        assertThat(lockAnnotation).isNotNull();
        assertThat(lockAnnotation.value())
                .isEqualTo(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

        assertThat(queryHintsAnnotation).isNotNull();
        assertThat(queryHintsAnnotation.value()).isNotEmpty();
        assertThat(queryHintsAnnotation.value()[0].value()).isEqualTo("3000");
    }

    @Test
    @DisplayName("[설정] LOCK_TIMEOUT 상수가 3000ms(3초)로 선언되어 있다")
    void pessimisticLock_timeoutConstant() throws Exception {
        var field = GroupRepository.class.getField("LOCK_TIMEOUT");
        field.setAccessible(true);
        String timeout = (String) field.get(null);
        assertThat(timeout).isEqualTo("3000");
    }
}


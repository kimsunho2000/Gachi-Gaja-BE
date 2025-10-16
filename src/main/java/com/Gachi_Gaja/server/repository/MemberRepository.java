package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    Optional<Member> findByGroupAndUser(Group group, User user);
    List<Member> findAllByGroup(Group group);
    long countByGroup(Group group);

    List<Member> findByGroup_GroupId(UUID groupId);
    List<Member> findByUser_UserId(UUID userId);
}

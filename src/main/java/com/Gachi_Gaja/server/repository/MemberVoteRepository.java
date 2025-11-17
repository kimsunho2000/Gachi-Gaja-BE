package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.MemberVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberVoteRepository extends JpaRepository<MemberVote, UUID> {
    Optional<MemberVote> findByUser_UserIdAndGroup_GroupId(UUID userId, UUID groupId);
    void deleteByUser_UserIdAndGroup_GroupId(UUID userId, UUID groupId);
}
package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.CandidatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface CandidatePlanRepository extends JpaRepository<CandidatePlan, UUID> {
    List<CandidatePlan> findAllByGroup_GroupId(UUID groupId);
}

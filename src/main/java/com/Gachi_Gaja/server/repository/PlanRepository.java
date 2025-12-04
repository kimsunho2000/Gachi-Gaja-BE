package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    boolean existsByGroup_GroupId(UUID groupId);
}
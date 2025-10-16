package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.CandidatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CandidatePlanRepository extends JpaRepository<CandidatePlan, UUID> {
}

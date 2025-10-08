package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, UUID> {


}

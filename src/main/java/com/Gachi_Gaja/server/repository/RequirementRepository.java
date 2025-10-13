package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Requirement;
import com.Gachi_Gaja.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, UUID> {

    boolean existsByUserAndGroup(User user, Group group);
    List<Requirement> findAllByGroup_GroupId(UUID groupId);

}

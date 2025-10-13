package com.Gachi_Gaja.server.repository;

import com.Gachi_Gaja.server.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

}

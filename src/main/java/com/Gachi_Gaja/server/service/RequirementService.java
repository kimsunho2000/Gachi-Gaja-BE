package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.dto.response.RequirementResponseDTO;
import com.Gachi_Gaja.server.repository.RequirementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementRepository requirementRepository;


    @Transactional(readOnly = true)
    public RequirementResponseDTO getRequirement(UUID groupId, UUID requirementId) {


    }


    // 추후에 세션에 있는 유저 정보와 대조하는 검증 로직 추가
    @Transactional
    public void deleteRequirement(UUID groupId, UUID requirementId) {
        if (!requirementRepository.existsById(requirementId)) {
            throw new EntityNotFoundException("Requirement not found: " + requirementId);
        }
        requirementRepository.deleteById(requirementId);
    }

}

package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.CafeInfoDTO;
import java.util.List;

/*
    카페 정보 조회시 응답 DTO
 */
public record CafeResponseDTO(
        List<CafeInfoDTO> cafeInfoList
) {}

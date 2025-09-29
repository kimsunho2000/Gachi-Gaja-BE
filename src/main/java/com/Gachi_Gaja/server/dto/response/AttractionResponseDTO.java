package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.AttractionInfoDTO;
import java.util.List;

/*
    관광 정보 조회시 응답 DTO
 */
public record AttractionResponseDTO(
        List<AttractionInfoDTO> attractionInfoList
) {}

package com.Gachi_Gaja.server.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class MemberInfoDTO {
    private UUID memberId;
    private UUID userId;
    private String nickname;
    private String role;
}
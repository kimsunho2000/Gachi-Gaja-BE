package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.GroupInfoDTO;
import java.util.List;

public record GroupListResponseDTO(boolean exists, List<GroupInfoDTO> groupList) {

    public static GroupListResponseDTO from(List<GroupInfoDTO> groupList) {
        boolean exists = groupList != null && !groupList.isEmpty();
        return new GroupListResponseDTO(exists, groupList);
    }

}

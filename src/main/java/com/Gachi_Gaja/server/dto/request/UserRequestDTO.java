package com.Gachi_Gaja.server.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
    회원 가입 및 회원 정보 수정시 DTO
 */
public class UserRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$") // 영문, 숫자, 특수문자가 모두 있는지 검사하는 정규식
    @Size(min = 8, max = 16)
    private String password;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$") // 한글, 영문, 숫자만 포함하는지 검사하는 정규식
    @Size(max = 8)
    private String nickName;

}

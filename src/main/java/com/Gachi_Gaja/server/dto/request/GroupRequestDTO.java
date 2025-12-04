package com.Gachi_Gaja.server.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.FutureOrPresent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
    그룹 생성, 수정시 DTO
 */
public class GroupRequestDTO {

    static final int MIN_BUDGET = 10000;

    @NotBlank
    private String title;

    @NotBlank
    private String region;

    @NotBlank
    private String startingPlace;

    @NotBlank
    private String endingPlace;

    @NotNull
    private LocalDate startingDay;

    @NotNull
    private LocalDate endingDay;

    @NotBlank
    private String transportation;

    @NotBlank
    @Pattern(regexp = "^(?<nights>0|[1-9]\\d*)\\s*박\\s*(?<days>[1-9]\\d*)\\s*일$")
    private String period;

    @Min(MIN_BUDGET)
    private int budget;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate rDeadline;

}

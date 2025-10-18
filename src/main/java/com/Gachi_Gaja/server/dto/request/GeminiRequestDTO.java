package com.Gachi_Gaja.server.dto.request;

import lombok.*;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
여행 계획 생성 시 DTO
 */
public class GeminiRequestDTO {

    private List<Content> contents;
    private GenerationConfig generationConfig;

    @Setter
    @Getter
    public static class Content {
        private Parts parts;
    }

    @Setter
    @Getter
    public static class Parts {
        private String text;    // 프롬프트
    }

    @Setter
    @Getter
    public static class GenerationConfig {
        private int candidate_count;    // 응답 후보
        private int max_output_tokens;  // 최대 응답 길이
        private double temperature;
    }

    public GeminiRequestDTO(String prompt, int candidateCount) {
        Parts parts = new Parts();
        parts.setText(prompt);
        Content content = new Content();
        content.setParts(parts);
        this.contents = new ArrayList<>();
        this.contents.add(content);

        this.generationConfig = new GenerationConfig();
        this.generationConfig.setCandidate_count(candidateCount);
        this.generationConfig.setMax_output_tokens(1000000);
        this.generationConfig.setTemperature(0.7);
    }

}

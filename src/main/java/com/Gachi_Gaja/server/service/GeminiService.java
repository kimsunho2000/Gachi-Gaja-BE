package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.configure.RestTemplateConfigure;
import com.Gachi_Gaja.server.dto.request.GeminiRequestDTO;
import com.Gachi_Gaja.server.dto.response.GeminiResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/*
Gemini 연동 서비스
 */
public class GeminiService {

    @Value("${gemini.model.name}")
    private String model;
    @Value("${gemini.api.url}")
    private String baseUrl;
    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplateConfigure restTemplateConfigure;

    /*
    프롬프트에 대한 답변을 생성하는 메서드
    prompt 프롬프트
    return 답변
     */
    public List<String> generateContent(String prompt, int candidateCount) {
        // request 설정
        GeminiRequestDTO request = new GeminiRequestDTO(prompt, candidateCount);

        // 요청 전송 준비
        String url = baseUrl + model + ":generateContent?key=" + apiKey;  // url

        RestTemplate template = restTemplateConfigure.geminiTemplate();
        GeminiResponseDTO response;
        response = template.postForObject(url, request, GeminiResponseDTO.class); // response

        // 요청 전송
        List<String> contents = response.getCandidates()
                .stream()
                .map(candidate -> candidate.getContent().getParts().get(0).getText().toString())
                .collect(Collectors.toList());

        return contents;
    }

}

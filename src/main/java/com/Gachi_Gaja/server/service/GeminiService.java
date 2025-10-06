package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.configure.RestTemplateConfigure;
import com.Gachi_Gaja.server.dto.request.GeminiRequestDTO;
import com.Gachi_Gaja.server.dto.response.GeminiResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    public String generateContent(String prompt) {
        // request 설정
        GeminiRequestDTO request = new GeminiRequestDTO(prompt);

        // 요청 전송 준비
        String url = baseUrl + model + ":generateContent?key=" + apiKey;  // url

        RestTemplate template = restTemplateConfigure.geminiTemplate();
        GeminiResponseDTO response;
        response = template.postForObject(url, request, GeminiResponseDTO.class); // response

        // 요청 전송
        String content = response.getCandidates().get(0).getContent().getParts().get(0).getText().toString();

        return content;
    }

}

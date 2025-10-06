package com.Gachi_Gaja.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final GeminiService geminiService;

    /*
    여행 계획 생성 메서드
     */
    public void generatePlan() {
        // 모임 정보, 여행 계획 후보 가져오기

        // 프롬프트 생성
        String prompt = "당신은 여행 정보와 간략한 여행 일정을 바탕으로 최적의 여행 계획을 제시하는 여행 플래너입니다.\n" +
                "아래 정보를 토대로 최적의 여행 계획을 제시하라.\n" +
                "\n" +
                "[여행 정보]\n" +
                "- 모임 관계 : \n" +
                "- 여행지 : \n" +
                "- 시작 장소, 시간 : \n" +
                "- 종료 장소, 시간 : \n" +
                "- 교통 수단 : \n" +
                "- 여행 기간 : \n" +
                "- 인당 예산 :\n" +
                "- 인원 : \n" +
                "[여행 일정]\n" +
                "[출력 형식]\n" +
                "시작 시간 (yyyy-MM-ddTHH:mm 형식) / 끝나는 시간 / 장소 / 설명 / 이동 수단 (시간) / 인당 예상 비용\n" +
                "[출력 예시]\n" +
                "2025-10-23T11:00 / 2025-10-23T13:00 / 오씨 칼국수 / 칼국수 식사 / 도보 (10분) / 10000\n" +
                "[참고 사항]\n" +
                "- 출력 형식과 예시에 맞춰 간결히 출력하고 그 외는 출력 X";

        // Gemini 호출 및 답변 받기
        String planContent = geminiService.generateContent(prompt);

        // 여행 계획 생성

        // 여행 계획 저장

    }

    /*
    여행 계획 생성 테스트 메서드 (추후 삭제)
     */
    /*
    public String generatePlanTest() {
        // 프롬프트 생성
        String prompt = "당신은 여행 정보와 간략한 여행 일정을 바탕으로 최적의 여행 계획을 제시하는 여행 플래너입니다.\n" +
                "아래 정보를 토대로 최적의 여행 계획을 제시하라.\n" +
                "\n" +
                "\uD83D\uDCCC 1일차\n" +
                "- 오전 : 버스) 부산역 도착 후 GNB호텔 부산 이동 및 짐 보관\n" +
                "- 중식 : 도보) 할매가야밀면 (밀면)\n" +
                "- 오후 : 도보) BIFF 광장, 자갈치시장, 국제시장 탐방 및 쇼핑\n" +
                "- 석식 : 도보) 개미집 남포점 (낙곱새)\n" +
                "- 숙소 : 도보) GNB호텔 부산\n" +
                "\n" +
                "\uD83D\uDCCC 2일차\n" +
                "- 오전 : 버스) 감천문화마을 탐방\n" +
                "- 중식 : 버스) 송정3대국밥 (돼지국밥)\n" +
                "- 오후 : 도보) 전포카페거리 및 서면 지하상가 쇼핑\n" +
                "- 석식 : 도보) 맛나감자탕 서면본점 (감자탕)\n" +
                "- 숙소 : 도보) GNB호텔 부산\n" +
                "\n" +
                "\uD83D\uDCCC 3일차\n" +
                "- 오전 : 지하철) 해운대 해수욕장, 동백섬 산책\n" +
                "- 중식 : 도보) 해운대 원조할매국밥 (소고기국밥)\n" +
                "- 오후 : 지하철) 부산역 이동\n" +
                "- 종료 : 부산역 출발\n" +
                "\n" +
                "\uD83C\uDF1F 계획 설명\n" +
                "맛집 탐방과 도보 이동 선호에 맞춰 부산의 대표적인 미식 경험과 명소를 지역별로 묶어 효율적인 동선을 구성했습니다. 아침형과 저녁형 모두 만족할 수 있도록 균형 잡힌 일정을 제공하며, 쇼핑과 여유로운 산책 시간을 적절히 배치하여 알차면서도 편안한 여행이 되도록 했습니다. 숙소는 위치와 가격을 고려해 남포동에 거점을 마련했습니다.\n" +
                "[출력 형식]\n" +
                "시작 시간 (yyyy-MM-ddTHH:mm 형식) / 끝나는 시간 / 장소 / 설명 / 이동 수단 (시간) / 인당 예상 비용\n" +
                "[출력 예시]\n" +
                "2025-10-23T11:00 / 2025-10-23T13:00 / 오씨 칼국수 / 칼국수 식사 / 도보 (10분) / 10000\n" +
                "[참고 사항]\n" +
                "- 출력 형식과 예시에 맞춰 간결히 출력하고 그 외는 출력 X";

        // Gemini 호출 및 답변 받기
        String planContent = geminiService.generateContent(prompt);

        return planContent;
    }
    */

}

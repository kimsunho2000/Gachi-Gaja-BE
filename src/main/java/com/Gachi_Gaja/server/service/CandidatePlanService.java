package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.Repository.CandidatePlanRepository;
import com.Gachi_Gaja.server.domain.CandidatePlan;
import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.dto.response.CandidatePlanResponseDTO;
import com.Gachi_Gaja.server.dto.response.RequirementResponseDTO;
import com.Gachi_Gaja.server.dto.response.TotalRequirementResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidatePlanService {

    private final CandidatePlanRepository candidatePlanRepository;
    private final GeminiService geminiService;
    private final RequirementService requirementService;

    /*
    RequirementResponseDTO의 한 속성을 {category=count, ...} 형식의 map으로 변환해 반환하는 함수
     */
    public Map<String, Long> mappingCategoryAndCount(List<RequirementResponseDTO> requirements, Function<RequirementResponseDTO, String> category) {
        Map<String, Long> map = requirements.stream()
                .map(category)  // category에 해당하는 필드만 추출
                .filter(s -> s != null && !s.trim().isEmpty())     // 입력하지 않은 경우 제외
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));     // {category=count, ...} 형식으로 변환

        return map;
    }

    /*
    여행 계획 후보 생성 메서드
     */
    @Transactional
    public void generateCandidatePlan(UUID groupId) {
        // 모임 정보 가져오기
        Group group = null;

        // 요구사항 가져오기
        TotalRequirementResponseDTO requirementInfo = requirementService.getRequirement(groupId);
        List<RequirementResponseDTO> requirements = requirementInfo.requirements();

        int totalMembers = requirementInfo.totalMembers();

        Map<String, Long> styles = mappingCategoryAndCount(requirements, RequirementResponseDTO::style);
        Map<String, Long> schedules = mappingCategoryAndCount(requirements, RequirementResponseDTO::schedule);
        Map<String, Long> lodgingCriterias = mappingCategoryAndCount(requirements, RequirementResponseDTO::lodgingCriteria);
        Map<String, Long> lodgingTypes = mappingCategoryAndCount(requirements, RequirementResponseDTO::lodgingType);
        Map<String, Long> mealBudgets = mappingCategoryAndCount(requirements, RequirementResponseDTO::mealBudget);
        Map<String, Long> eatingHabits = mappingCategoryAndCount(requirements, RequirementResponseDTO::eatingHabit);
        Map<String, Long> distances = mappingCategoryAndCount(requirements, RequirementResponseDTO::distance);
        Map<String, Long> plusRequirements = mappingCategoryAndCount(requirements, RequirementResponseDTO::plusRequirement);


        // 프롬프트 생성
        String prompt = "당신은 여행 정보와 요구 사항을 바탕으로 최적의 여행 계획을 제시하는 여행 플래너입니다.\n" +
                "아래 정보를 토대로 최적의 여행 계획을 제시하라.\n" +
                "[여행 정보]\n" +
                "- 모임 관계 : \n" +
                "- 여행지 : \n" +
                "- 시작 장소, 시간 : \n" +
                "- 종료 장소, 시간 : \n" +
                "- 교통 수단 : \n" +
                "- 여행 기간 : \n" +
                "- 인당 예산 :\n" +
                "- 인원 : " + totalMembers + "\n" +
                "[요구 사항]\n" +
                "- 여행 스타일 : " + styles.toString() + "\n" +
                "- 일정 구성 : " + schedules.toString() + "\n" +
                "- 숙소 선택 기준 : " + lodgingCriterias.toString() + "\n" +
                "- 숙소 종류 : " + lodgingTypes.toString() + "\n" +
                "- 식사 예산 : " + mealBudgets.toString() + "\n" +
                "- 식습관 : " + eatingHabits.toString() + "\n" +
                "- 이동 거리 : " + distances.toString() + "\n" +
                "- 추가 요구 사항 : " + plusRequirements.toString() + "\n" +
                "[출력 예시]\n" +
                "📌 1일차\n" +
                "- 오전 : 대전역 도착\n" +
                "- 중식 : 택시) 오씨 칼국수 (칼국수)\n" +
                "- 오후 : 도보) 성심당 본점 및 케익 부띠끄 방문\n" +
                "- 석식 : 버스) 태평소 국밥 택시 이동 및 소고기 국밥, 육사시미 식사\n" +
                "- 숙소 : 도보) 토요 코인 호텔\n" +
                "\n" +
                "🌟 계획 설명\n" +
                "- 여유로운 일정 선호를 반영했어요!\n" +
                "[참고 사항]\n" +
                "- 키워드 옆 숫자가 높을 수록 가중치 증가 (1명일 시 숫자 표기 X)\n" +
                "- 동일 가중치의 키워드가 있을 시 일별 적용\n" +
                "- 실제 운영 중인 식당, 관광지, 숙소로 일정 구성\n" +
                "- 식당, 관광지, 숙소명 구체적으로 기제\n" +
                "- 메뉴, 관광지 겹치지 않게 구성\n" +
                "[출력 형식]\n" +
                "- 출력 예시에 맞춰 간결히 출력하고 그 외는 출력 X\n" +
                "- 계획 설명은 해당 계획을 세운 근거를 100자 내외로 작성";

        // Gemini 호출 및 답변 받기
        String planContent = geminiService.generateContent(prompt);

        // 여행 계획 후보 생성
        CandidatePlan candidatePlan = new CandidatePlan(group, planContent, 0, false);

        // 여행 계획 후보 저장
        candidatePlanRepository.save(candidatePlan);
    }

    /*
    여행 계획 후보 전체 조회 메서드
     */
    public List<CandidatePlanResponseDTO> findByAll() {
        return null;
    }

    /*
    여행 계획 후보 단일 조회 메서드
     */
    @Transactional
    public CandidatePlanResponseDTO findById(UUID id) {
        CandidatePlan candidatePlan = candidatePlanRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("여행 계획을 찾을 수 없습니다."));

        return CandidatePlanResponseDTO.from(candidatePlan);
    }

    /*
    여행 계획 후보 삭제 메서드
     */
    @Transactional
    public void delete(UUID id) {
        candidatePlanRepository.deleteById(id);
    }

}

package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.domain.*;
import com.Gachi_Gaja.server.domain.CandidatePlan;
import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.Plan;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.dto.PlanInfoDTO;
import com.Gachi_Gaja.server.dto.request.PlanRequestDTO;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.PlanRepository;
import com.Gachi_Gaja.server.dto.response.PlanResponseDTO;
import com.Gachi_Gaja.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    private final GeminiService geminiService;

    /*
    여행 계획 생성 메서드
     */
    @Transactional
    public void generatePlan(UUID groupId, UUID userId) {
        // 모임 가져오기
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        // 리더 확인 (리더가 아닐 시 생성 불가)
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Member member = memberRepository.findByGroupAndUser(group, user).orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없습니다."));

        if (member.isLeader() != true)
            throw new IllegalArgumentException("리더만 여행 계획을 생성할 수 있습니다.");

        // 여행 계획 후보 가져오기
        CandidatePlan candidatePlan = group.getCandidatePlans().get(0);
        String candidatePlanContent = candidatePlan.getPlanContent();

        // 프롬프트 생성
        String prompt = "당신은 여행 정보와 간략한 여행 일정을 바탕으로 최적의 여행 계획을 제시하는 여행 플래너입니다.\n" +
                "아래 정보를 토대로 최적의 여행 계획을 제시하라.\n" +
                "\n" +
                "[여행 정보]\n" +
                "- 여행지 : " + group.getRegion() + "\n" +
                "- 시작 장소 : " + group.getStartingPoint() + ", " + "\n"  +
                "- 종료 장소 : " + group.getEndingPoint() + "\n" +
                "- 교통 수단 : " + group.getTransportation() + "\n" +
                "- 여행 기간 : " + group.getPeriod() + "\n" +
                "- 인당 예산: " + group.getBudget() + "\n" +
                "- 인원 : " + group.getMembers().size() + "\n" +
                "[여행 일정]\n" + candidatePlanContent + "\n" +
                "[출력 형식]\n" +
                "시작 시간 (yyyy-MM-ddTHH:mm 형식) / 끝나는 시간 / 장소 / 설명 / 이동 수단 (시간) / 인당 예상 비용\n" +
                "[출력 예시]\n" +
                "2025-10-23T11:00 / 2025-10-23T13:00 / 오씨 칼국수 / 칼국수 식사 / 도보 (10분) / 10000\n" +
                "2025-10-23T13:00 / 2025-10-23T17:00 / 성심당, 성심당 케익부띠끄 / 빵 쇼핑 / 도보 (10분) / 30000\n" +
                "[참고 사항]\n" +
                "- 출력 형식과 예시에 맞춰 간결히 출력하고 그 외는 출력 X";

        // Gemini 호출 및 답변 받기
        String planContent = geminiService.generateContent(prompt, 1).get(0);

        // 여행 계획 생성
        List<Plan> plans = planContent.lines()     // 줄 단위 구분
                .filter(line -> !line.trim().isEmpty())     // 빈 줄 제거
                .map(line -> line.split(" / "))     // ' / ' 으로 분리
                .map(fields -> {
                    // 요소 추출
                    LocalDateTime startingTime = LocalDateTime.parse(fields[0].trim());
                    LocalDateTime endingTime = LocalDateTime.parse(fields[1].trim());
                    String location = fields[2].trim();
                    String info = fields[3].trim();
                    String transportation = fields[4].trim();
                    int cost = Integer.parseInt(fields[5].trim());

                    return Plan.builder()
                            .group(group)
                            .startingTime(startingTime)
                            .endingTime(endingTime)
                            .location(location)
                            .info(info)
                            .transportation(transportation)
                            .cost(cost)
                            .build();
                })  // Plan으로 변환
                .collect(Collectors.toList());  // Plan list 반환

        // 여행 계획 저장
        planRepository.saveAll(plans);
    }

    /*
    여행 계획 전체 조회
     */
    @Transactional
    public PlanResponseDTO findAll(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        List<PlanInfoDTO> planInfos = group.getPlans().stream()
                .sorted(Comparator.comparing(Plan::getStartingTime))    // 시간순 정렬
                .map(plan -> PlanInfoDTO.from(plan))
                .collect(Collectors.toList());

        PlanResponseDTO plans = PlanResponseDTO.builder()
                .isLeader(true)
                .planList(planInfos)
                .build();

        return plans;
    }

    /*
    여행 계획 수정 메서드
     */
    @Transactional
    public void update(UUID groupId, UUID planId, UUID userId, PlanRequestDTO request) {
        // 모임 가져오기
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        // 리더 확인 (리더가 아닐 시 생성 불가)
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Member member = memberRepository.findByGroupAndUser(group, user).orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없습니다."));

        if (member.isLeader() != true)
            throw new IllegalArgumentException("리더만 여행 계획을 수정할 수 있습니다.");

        // 여행 계획 수정
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new EntityNotFoundException("여행 계획을 찾을 수 없습니다."));
        plan.update(request);
    }

    /*
    여행 계획 추가 메서드
     */
    @Transactional
    public void add(UUID groupId, UUID userId, PlanRequestDTO request) {
        // 모임 가져오기
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        // 리더 확인 (리더가 아닐 시 생성 불가)
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Member member = memberRepository.findByGroupAndUser(group, user).orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없습니다."));

        if (member.isLeader() != true)
            throw new IllegalArgumentException("리더만 여행 계획을 추가할 수 있습니다.");

        // 여행 계획 추가
        planRepository.save(request.toEntity(group));
    }

    /*
    여행 계획 삭제 메서드
     */
    @Transactional
    public void delete(UUID groupId, UUID planId, UUID userId) {
        // 모임 가져오기
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        // 리더 확인 (리더가 아닐 시 생성 불가)
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Member member = memberRepository.findByGroupAndUser(group, user).orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없습니다."));

        if (member.isLeader() != true)
            throw new IllegalArgumentException("리더만 여행 계획을 삭제할 수 있습니다.");

        // 여행 계획 삭제
        planRepository.deleteById(planId);
    }

}

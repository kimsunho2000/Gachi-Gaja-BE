package com.Gachi_Gaja.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`groups`") // 복수형으로 변경 - 이것도 에러가 나서 백틱 추가해 수정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "group_id")
    private UUID groupId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String region;

    @Column(name = "starting_point", nullable = false)
    private String startingPoint;

    @Column(name = "ending_point", nullable = false)
    private String endingPoint;

    @Column(nullable = false)
    private String transportation;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false)
    private int budget;

    @Column(name = "requirement_deadline")
    private LocalDate requirementDeadline;

    @Column(name = "vote_deadline")
    private LocalDate voteDeadline;

    @Column(name = "call_count")
    private int callCnt;    // AI 호출 횟수

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Requirement> requirements = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CandidatePlan> candidatePlans = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Plan> plans = new ArrayList<>();

    @Builder
    public Group(String title, String region, String startingPoint, String endingPoint,
                 String transportation, String period, int budget,
                 LocalDate requirementDeadline, LocalDate voteDeadline, int callCnt) {
        this.title = title;
        this.region = region;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.transportation = transportation;
        this.period = period;
        this.budget = budget;
        this.requirementDeadline = requirementDeadline;
        this.voteDeadline = voteDeadline;
        this.callCnt = callCnt;
    }

    /*
    여행 계획 후보 생성 시 투표 마감일 설정
     */
    public void setVoteDeadline(LocalDate deadline) {
        this.voteDeadline = deadline;
    }

    /*
    여행 계획 후보 생성 시 AI 호출 횟수 감소
     */
    public void decreaseCallCnt() {
        callCnt -= 1;
    }

}

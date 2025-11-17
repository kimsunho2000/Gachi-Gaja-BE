package com.Gachi_Gaja.server.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "member_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_vote_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_plan_id", nullable = false)
    private CandidatePlan candidatePlan;

    // 🔥 Builder 생성자(필드 모두 포함)
    @Builder
    public MemberVote(User user, Group group, CandidatePlan candidatePlan) {
        this.user = user;
        this.group = group;
        this.candidatePlan = candidatePlan;
    }

    public void setCandidatePlan(CandidatePlan candidatePlan) {
        this.candidatePlan = candidatePlan;
    }
}
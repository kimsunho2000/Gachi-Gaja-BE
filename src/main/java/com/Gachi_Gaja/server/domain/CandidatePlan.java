package com.Gachi_Gaja.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "candidate_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidatePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "candidate_plan_id")
    private UUID candidatePlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, name = "plan_content", columnDefinition = "LONGTEXT")
    private String planContent;

    @Column(nullable = false, name = "vote_count") //
    private int voteCount;

    @Column(nullable = false, name = "is_voted") //
    private boolean isVoted;

    @Builder
    public CandidatePlan(Group group, String planContent, int voteCount, boolean isVoted) {
        this.group = group;
        this.planContent = planContent;
        this.voteCount = voteCount;
        this.isVoted = isVoted;
    }

    public void updateVoted(boolean voted) {
        this.isVoted = voted;
    }

    public void updateVoteCount(int count) {
        this.voteCount = count;
    }
}

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
@Table(name = "groups")
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

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Requirement> requirements = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CandidatePlan> candidatePlans = new ArrayList<>();

    @Builder
    public Group(String title, String region, String startingPoint, String endingPoint,
                 String transportation, String period, int budget,
                 LocalDate requirementDeadline, LocalDate voteDeadline) {
        this.title = title;
        this.region = region;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.transportation = transportation;
        this.period = period;
        this.budget = budget;
        this.requirementDeadline = requirementDeadline;
        this.voteDeadline = voteDeadline;
    }
}

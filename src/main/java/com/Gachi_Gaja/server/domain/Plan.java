package com.Gachi_Gaja.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_id")
    private UUID planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "starting_time", nullable = false)
    private LocalDateTime startingTime;

    @Column(name = "ending_time", nullable = false)
    private LocalDateTime endingTime;

    @Column(nullable = false)
    private String location;

    @Column()
    private String info;

    @Column(nullable = false)
    private String transportation;

    @Column(nullable = false)
    private int cost;

    @Builder
    public Plan(Group group, LocalDateTime startingTime, LocalDateTime endingTime,
                String location, String info, String transportation, int cost) {
        this.group = group;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.location = location;
        this.info = info;
        this.transportation = transportation;
        this.cost = cost;
    }

}

package com.Gachi_Gaja.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "attractions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attraction_id")
    private UUID attractionId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String location;

    @Column
    private String info;

    @Builder
    public Attraction(String name, String region, String location, String info) {
        this.name = name;
        this.region = region;
        this.location = location;
        this.info = info;
    }
}

package com.Gachi_Gaja.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cafes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cafe_id")
    private UUID cafeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String location;

    @Column
    private String info;

    @ManyToMany(mappedBy = "cafes",fetch = FetchType.LAZY)
    private List<Requirement> requirements = new ArrayList<>();

    @Builder
    public Cafe(String name, String region, String location, String info) {
        this.name = name;
        this.region = region;
        this.location = location;
        this.info = info;
    }
}

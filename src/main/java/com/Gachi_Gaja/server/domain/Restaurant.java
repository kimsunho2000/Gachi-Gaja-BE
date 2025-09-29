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
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String location;

    @Column
    private String info;

    @ManyToMany(mappedBy = "restaurants", fetch = FetchType.LAZY)
    private List<Requirement> requirements = new ArrayList<>();

    @Builder
    public Restaurant(String name, String region, String location, String info) {
        this.name = name;
        this.region = region;
        this.location = location;
        this.info = info;
    }

}

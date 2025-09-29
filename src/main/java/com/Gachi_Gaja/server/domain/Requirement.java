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
@Table(name = "requirements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Requirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "requirement_id")
    private UUID requirementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "style")
    private String style;

    @Column(name = "schedule")
    private String schedule;

    @Column(name = "lodging_criteria")
    private String lodgingCriteria;

    @Column(name = "lodging_type")
    private String lodgingType;

    @Column(name = "meal_budget")
    private String mealBudget;

    @Column(name = "eating_habit")
    private String eatingHabit;

    @Column()
    private String distance;

    @Column(name = "plus_requirement")
    private String plusRequirement;

    @ManyToMany
    @JoinTable(
            name = "requirement_restaurant",
            joinColumns = @JoinColumn(name = "requirement_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> restaurants = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "requirement_attraction",
            joinColumns = @JoinColumn(name = "requirement_id"),
            inverseJoinColumns = @JoinColumn(name = "attraction_id")
    )
    private List<Attraction> attractions = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "requirement_cafe",
            joinColumns = @JoinColumn(name = "requirement_id"),
            inverseJoinColumns = @JoinColumn(name = "cafe_id")
    )
    private List<Cafe> cafes = new ArrayList<>();

    @Builder
    public Requirement(Group group, User user, String style, String schedule,
                      String lodgingCriteria, String lodgingType, String mealBudget,
                      String eatingHabit, String distance, String plusRequirement,
                      List<Restaurant> restaurants, List<Attraction> attractions, List<Cafe> cafes) {
        this.group = group;
        this.user = user;
        this.style = style;
        this.schedule = schedule;
        this.lodgingCriteria = lodgingCriteria;
        this.lodgingType = lodgingType;
        this.mealBudget = mealBudget;
        this.eatingHabit = eatingHabit;
        this.distance = distance;
        this.plusRequirement = plusRequirement;
        this.restaurants = restaurants;
        this.attractions = attractions;
        this.cafes = cafes;
    }
}


package com.example.path.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_option")
public class RouteOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_search_id", nullable = false)
    private RouteSearch routeSearch;

    @Column(nullable = false)
    private Integer optionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteType routeType;

    @Column(nullable = false)
    private Integer distanceMeters;

    @Column(nullable = false)
    private Integer durationSeconds;

    private Integer estimatedFare;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String polyline;

    @Column(length = 30, nullable = false)
    private String provider;
}
/*
public Long getId();
public RouteSearch getRouteSearch();
public Integer getOptionNumber();
public RouteType getRouteType();
public Integer getDistanceMeters();
public Integer getDurationSeconds();
public String getProvider();
* */
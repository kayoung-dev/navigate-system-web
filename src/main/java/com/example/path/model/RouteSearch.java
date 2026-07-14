package com.example.path.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.lang.reflect.Member;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_search")
public class RouteSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "name",
                    column = @Column(name = "start_name")
            ),
            @AttributeOverride(
                    name = "address",
                    column = @Column(name = "start_address")
            ),
            @AttributeOverride(
                    name = "latitude",
                    column = @Column(name = "start_latitude")
            ),
            @AttributeOverride(
                    name = "longitude",
                    column = @Column(name = "start_longitude")
            )
    })
    private Location startLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "name",
                    column = @Column(name = "destination_name")
            ),
            @AttributeOverride(
                    name = "address",
                    column = @Column(name = "destination_address")
            ),
            @AttributeOverride(
                    name = "latitude",
                    column = @Column(name = "destination_latitude")
            ),
            @AttributeOverride(
                    name = "longitude",
                    column = @Column(name = "destination_longitude")
            )
    })
    private Location destinationLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportMode transportMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelPurpose purpose;

    @Column(nullable = false)
    private LocalDateTime searchedAt;
/*
    public Long getId();
    public Location getStartLocation();
    public Location getDestinationLocation();
    public TransportMode getTransportMode();
    public TravelPurpose getPurpose();

 */
}
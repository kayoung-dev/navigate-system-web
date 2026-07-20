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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Table(name = "route_search")
public class RouteSearch {

    @EqualsAndHashCode.Include
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
}
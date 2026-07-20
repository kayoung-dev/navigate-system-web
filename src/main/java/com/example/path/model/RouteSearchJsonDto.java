package com.example.path.model;

import java.time.LocalDateTime;

/**
 * route_search.json에 저장되는 한 행의 모양을 나타내는 DTO.
 */
public record RouteSearchJsonDto(
        Long id,
        LocationDto startLocation,
        LocationDto destinationLocation,
        TransportMode transportMode,
        TravelPurpose purpose,
        LocalDateTime searchedAt
) {

    public static RouteSearchJsonDto from(RouteSearch routeSearch, Long id) {
        return new RouteSearchJsonDto(
                id,
                LocationDto.from(routeSearch.getStartLocation()),
                LocationDto.from(routeSearch.getDestinationLocation()),
                routeSearch.getTransportMode(),
                routeSearch.getPurpose(),
                routeSearch.getSearchedAt()
        );
    }

    public RouteSearch toEntity() {
        return RouteSearch.builder()
                .id(id)
                .startLocation(startLocation.toLocation())
                .destinationLocation(destinationLocation.toLocation())
                .transportMode(transportMode)
                .purpose(purpose)
                .searchedAt(searchedAt)
                .build();
    }

    public record LocationDto(String name, String address, Double latitude, Double longitude) {

        public static LocationDto from(Location location) {
            return new LocationDto(
                    location.getName(),
                    location.getAddress(),
                    location.getLatitude(),
                    location.getLongitude()
            );
        }

        public Location toLocation() {
            return new Location(name, address, latitude, longitude);
        }
    }
}

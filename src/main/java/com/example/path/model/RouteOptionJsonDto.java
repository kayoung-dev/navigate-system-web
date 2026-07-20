package com.example.path.model;

/**
 * route_option.json에 저장되는 한 행의 모양을 나타내는 DTO.
 * RouteOption.routeSearch(FK)는 중첩 객체 대신 routeSearchId로 평탄화해서 순환 참조를 피한다.
 */
public record RouteOptionJsonDto(
        Long id,
        Long routeSearchId,
        Integer optionNumber,
        RouteType routeType,
        Integer distanceMeters,
        Integer durationSeconds,
        Integer estimatedFare,
        String polyline,
        String provider
) {

    public static RouteOptionJsonDto from(RouteOption routeOption, Long id) {
        return new RouteOptionJsonDto(
                id,
                routeOption.getRouteSearch().getId(),
                routeOption.getOptionNumber(),
                routeOption.getRouteType(),
                routeOption.getDistanceMeters(),
                routeOption.getDurationSeconds(),
                routeOption.getEstimatedFare(),
                routeOption.getPolyline(),
                routeOption.getProvider()
        );
    }

    public RouteOption toEntity(RouteSearch routeSearch) {
        return RouteOption.builder()
                .id(id)
                .routeSearch(routeSearch)
                .optionNumber(optionNumber)
                .routeType(routeType)
                .distanceMeters(distanceMeters)
                .durationSeconds(durationSeconds)
                .estimatedFare(estimatedFare)
                .polyline(polyline)
                .provider(provider)
                .build();
    }
}

package com.example.path.repository;

import com.example.path.model.RouteSearch;
import com.example.path.model.TransportMode;
import com.example.path.model.TravelPurpose;
import java.time.LocalDateTime;
import java.util.List;

public interface RouteSearchRepository {

    void save(RouteSearch routeSearch);

    List<RouteSearch> findAll();

    RouteSearch findById(Long id);

    void update(RouteSearch routeSearch);

    void delete(Long id);

    /**
     * 출발지·도착지 이름 또는 주소에 포함된 검색어로 조회합니다.
     */
    List<RouteSearch> findByKeyword(String keyword);

    List<RouteSearch> findByPurpose(TravelPurpose purpose);

    List<RouteSearch> findByTransportMode(TransportMode transportMode);

    List<RouteSearch> findBySearchedAtBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );


}

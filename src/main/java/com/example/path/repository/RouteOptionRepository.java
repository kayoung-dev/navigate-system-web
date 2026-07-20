package com.example.path.repository;

import com.example.path.model.RouteOption;
import com.example.path.model.RouteType;
import java.util.List;

public interface RouteOptionRepository {

    RouteOption save(RouteOption routeOption);

    List<RouteOption> saveAll(List<RouteOption> routeOptions);

    List<RouteOption> findAll();

    RouteOption findById(Long id);

    void update(RouteOption routeOption);

    void delete(Long id);

    /**
     * 하나의 길찾기 요청에서 조회된 모든 경로 옵션을 반환합니다.
     */
    List<RouteOption> findByRouteSearchId(Long routeSearchId);

    List<RouteOption> findByRouteType(RouteType routeType);

    List<RouteOption> findByProvider(String provider);

    /**
     * 길찾기 검색 기록에 연결된 경로 옵션을 모두 삭제합니다.
     */
    void deleteByRouteSearchId(Long routeSearchId);
}
package com.example.path.service;

import com.example.path.model.RouteSearch;
import com.example.path.model.TransportMode;
import com.example.path.model.TravelPurpose;
import com.example.path.repository.RouteSearchRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class RouteSearchService {

    private final RouteSearchRepository repository;
    private final RouteOptionService routeOptionService;

    public RouteSearchService(RouteSearchRepository repository, RouteOptionService routeOptionService) {
        this.repository = repository;
        this.routeOptionService = routeOptionService;
    }

    /**
     * 새로운 길찾기 검색 기록을 저장합니다. searchedAt이 비어 있으면 현재 시각으로 채웁니다.
     */
    public RouteSearch save(RouteSearch routeSearch) {
        validateRouteSearch(routeSearch);

        if (routeSearch.getSearchedAt() == null) {
            routeSearch = routeSearch.toBuilder()
                    .searchedAt(LocalDateTime.now())
                    .build();
        }

        return repository.save(routeSearch);
    }

    public List<RouteSearch> findAll() {
        return repository.findAll();
    }

    public RouteSearch findById(Long id) {
        validateId(id);

        RouteSearch routeSearch = repository.findById(id);

        if (routeSearch == null) {
            throw new EntityNotFoundException(
                    "길찾기 검색 기록을 찾을 수 없습니다. id=" + id
            );
        }

        return routeSearch;
    }

    /**
     * RouteSearch 객체의 ID가 존재해야 합니다.
     * 출발지/도착지는 변경할 수 없습니다(새로운 검색으로 등록해야 함) — transportMode/purpose만 변경 가능합니다.
     */
    public RouteSearch update(RouteSearch routeSearch) {
        validateRouteSearch(routeSearch);

        if (routeSearch.getId() == null) {
            throw new IllegalArgumentException(
                    "수정할 길찾기 검색 기록의 ID가 필요합니다."
            );
        }

        RouteSearch existing = findById(routeSearch.getId());
        validateUnchangedLocations(existing, routeSearch);

        repository.update(routeSearch);

        return routeSearch;
    }

    /**
     * 길찾기 검색 기록을 삭제합니다. 연관된 경로 옵션(RouteOption)도 함께 삭제됩니다.
     */
    public void delete(Long id) {
        findById(id);
        routeOptionService.deleteByRouteSearchId(id);
        repository.delete(id);
    }

    public List<RouteSearch> findByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return repository.findByKeyword(keyword.trim());
    }

    public List<RouteSearch> findByPurpose(TravelPurpose purpose) {
        Objects.requireNonNull(purpose, "이동 목적은 null일 수 없습니다.");

        return repository.findByPurpose(purpose);
    }

    public List<RouteSearch> findByTransportMode(
            TransportMode transportMode
    ) {
        Objects.requireNonNull(
                transportMode,
                "이동수단은 null일 수 없습니다."
        );

        return repository.findByTransportMode(transportMode);
    }

    public List<RouteSearch> findByPeriod(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        Objects.requireNonNull(
                startDateTime,
                "검색 시작 일시는 null일 수 없습니다."
        );
        Objects.requireNonNull(
                endDateTime,
                "검색 종료 일시는 null일 수 없습니다."
        );

        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException(
                    "검색 시작 일시는 종료 일시보다 늦을 수 없습니다."
            );
        }

        return repository.findBySearchedAtBetween(
                startDateTime,
                endDateTime
        );
    }

    private void validateUnchangedLocations(RouteSearch existing, RouteSearch routeSearch) {
        if (!existing.getStartLocation().equals(routeSearch.getStartLocation())
                || !existing.getDestinationLocation().equals(routeSearch.getDestinationLocation())) {
            throw new IllegalStateException(
                    "출발지/도착지는 변경할 수 없습니다. 새로운 검색으로 등록해주세요."
            );
        }
    }

    private void validateRouteSearch(RouteSearch routeSearch) {
        Objects.requireNonNull(
                routeSearch,
                "길찾기 검색 정보는 null일 수 없습니다."
        );

        Objects.requireNonNull(
                routeSearch.getStartLocation(),
                "출발지는 필수입니다."
        );

        Objects.requireNonNull(
                routeSearch.getDestinationLocation(),
                "도착지는 필수입니다."
        );

        Objects.requireNonNull(
                routeSearch.getTransportMode(),
                "이동수단은 필수입니다."
        );

        Objects.requireNonNull(
                routeSearch.getPurpose(),
                "이동 목적은 필수입니다."
        );
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "올바른 길찾기 검색 ID가 필요합니다."
            );
        }
    }
}

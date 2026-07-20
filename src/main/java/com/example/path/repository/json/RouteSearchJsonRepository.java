package com.example.path.repository.json;

import com.example.path.model.RouteSearch;
import com.example.path.model.RouteSearchJsonDto;
import com.example.path.model.TransportMode;
import com.example.path.model.TravelPurpose;
import com.example.path.repository.RouteSearchRepository;
import com.example.path.util.JsonFileConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * route_search.json 파일을 저장소로 사용하는 RouteSearchRepository 구현체.
 */
public class RouteSearchJsonRepository implements RouteSearchRepository {

    private static final Path FILE_PATH = Path.of("data", "route_search.json");

    private final JsonFileConnection dbConnection;

    public RouteSearchJsonRepository(JsonFileConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public RouteSearch save(RouteSearch routeSearch) {
        List<RouteSearchJsonDto> dtos = readAll();
        long newId = nextId(dtos);
        dtos.add(RouteSearchJsonDto.from(routeSearch, newId));
        dbConnection.writeAll(FILE_PATH, dtos);
        return routeSearch.toBuilder().id(newId).build();
    }

    @Override
    public List<RouteSearch> findAll() {
        return readAll().stream()
                .map(RouteSearchJsonDto::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public RouteSearch findById(Long id) {
        return readAll().stream()
                .filter(dto -> dto.id().equals(id))
                .findFirst()
                .map(RouteSearchJsonDto::toEntity)
                .orElse(null);
    }

    @Override
    public void update(RouteSearch routeSearch) {
        List<RouteSearchJsonDto> dtos = readAll().stream()
                .map(dto -> dto.id().equals(routeSearch.getId())
                        ? RouteSearchJsonDto.from(routeSearch, routeSearch.getId())
                        : dto)
                .collect(Collectors.toList());
        dbConnection.writeAll(FILE_PATH, dtos);
    }

    @Override
    public void delete(Long id) {
        List<RouteSearchJsonDto> dtos = readAll().stream()
                .filter(dto -> !dto.id().equals(id))
                .collect(Collectors.toList());
        dbConnection.writeAll(FILE_PATH, dtos);
    }

    @Override
    public List<RouteSearch> findByKeyword(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return findAll().stream()
                .filter(routeSearch -> matchesKeyword(routeSearch, lowerKeyword))
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteSearch> findByPurpose(TravelPurpose purpose) {
        return findAll().stream()
                .filter(routeSearch -> routeSearch.getPurpose() == purpose)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteSearch> findByTransportMode(TransportMode transportMode) {
        return findAll().stream()
                .filter(routeSearch -> routeSearch.getTransportMode() == transportMode)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteSearch> findBySearchedAtBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        return findAll().stream()
                .filter(routeSearch -> !routeSearch.getSearchedAt().isBefore(startDateTime)
                        && !routeSearch.getSearchedAt().isAfter(endDateTime))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(RouteSearch routeSearch, String lowerKeyword) {
        return containsIgnoreCase(routeSearch.getStartLocation().getName(), lowerKeyword)
                || containsIgnoreCase(routeSearch.getStartLocation().getAddress(), lowerKeyword)
                || containsIgnoreCase(routeSearch.getDestinationLocation().getName(), lowerKeyword)
                || containsIgnoreCase(routeSearch.getDestinationLocation().getAddress(), lowerKeyword);
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

    private List<RouteSearchJsonDto> readAll() {
        return dbConnection.readAll(FILE_PATH, new TypeReference<List<RouteSearchJsonDto>>() {
        });
    }

    private long nextId(List<RouteSearchJsonDto> dtos) {
        return dtos.stream().mapToLong(RouteSearchJsonDto::id).max().orElse(0) + 1;
    }
}

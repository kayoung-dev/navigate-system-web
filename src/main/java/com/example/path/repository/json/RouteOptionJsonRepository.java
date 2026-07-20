package com.example.path.repository.json;

import com.example.path.model.RouteOption;
import com.example.path.model.RouteOptionJsonDto;
import com.example.path.model.RouteSearch;
import com.example.path.model.RouteType;
import com.example.path.repository.RouteOptionRepository;
import com.example.path.repository.RouteSearchRepository;
import com.example.path.util.DBConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * route_option.json 파일을 저장소로 사용하는 RouteOptionRepository 구현체.
 * routeSearchId(FK)를 실제 RouteSearch로 복원하기 위해 RouteSearchRepository를 함께 사용한다.
 */
public class RouteOptionJsonRepository implements RouteOptionRepository {

    private static final Path FILE_PATH = Path.of("data", "route_option.json");

    private final DBConnection dbConnection;
    private final RouteSearchRepository routeSearchRepository;

    public RouteOptionJsonRepository(DBConnection dbConnection, RouteSearchRepository routeSearchRepository) {
        this.dbConnection = dbConnection;
        this.routeSearchRepository = routeSearchRepository;
    }

    @Override
    public RouteOption save(RouteOption routeOption) {
        List<RouteOptionJsonDto> dtos = readAll();
        long newId = nextId(dtos);
        dtos.add(RouteOptionJsonDto.from(routeOption, newId));
        dbConnection.writeAll(FILE_PATH, dtos);
        return routeOption.toBuilder().id(newId).build();
    }

    @Override
    public List<RouteOption> saveAll(List<RouteOption> routeOptions) {
        List<RouteOptionJsonDto> dtos = readAll();
        long newId = nextId(dtos);
        List<RouteOption> saved = new ArrayList<>();

        for (RouteOption routeOption : routeOptions) {
            dtos.add(RouteOptionJsonDto.from(routeOption, newId));
            saved.add(routeOption.toBuilder().id(newId).build());
            newId++;
        }

        dbConnection.writeAll(FILE_PATH, dtos);
        return saved;
    }

    @Override
    public List<RouteOption> findAll() {
        return readAll().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public RouteOption findById(Long id) {
        return readAll().stream()
                .filter(dto -> dto.id().equals(id))
                .findFirst()
                .map(this::toEntity)
                .orElse(null);
    }

    @Override
    public void update(RouteOption routeOption) {
        List<RouteOptionJsonDto> dtos = readAll().stream()
                .map(dto -> dto.id().equals(routeOption.getId())
                        ? RouteOptionJsonDto.from(routeOption, routeOption.getId())
                        : dto)
                .collect(Collectors.toList());
        dbConnection.writeAll(FILE_PATH, dtos);
    }

    @Override
    public void delete(Long id) {
        List<RouteOptionJsonDto> dtos = readAll().stream()
                .filter(dto -> !dto.id().equals(id))
                .collect(Collectors.toList());
        dbConnection.writeAll(FILE_PATH, dtos);
    }

    @Override
    public List<RouteOption> findByRouteSearchId(Long routeSearchId) {
        return readAll().stream()
                .filter(dto -> dto.routeSearchId().equals(routeSearchId))
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteOption> findByRouteType(RouteType routeType) {
        return findAll().stream()
                .filter(routeOption -> routeOption.getRouteType() == routeType)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteOption> findByProvider(String provider) {
        return findAll().stream()
                .filter(routeOption -> routeOption.getProvider() != null
                        && routeOption.getProvider().equalsIgnoreCase(provider))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByRouteSearchId(Long routeSearchId) {
        List<RouteOptionJsonDto> dtos = readAll().stream()
                .filter(dto -> !dto.routeSearchId().equals(routeSearchId))
                .collect(Collectors.toList());
        dbConnection.writeAll(FILE_PATH, dtos);
    }

    private RouteOption toEntity(RouteOptionJsonDto dto) {
        RouteSearch routeSearch = routeSearchRepository.findById(dto.routeSearchId());
        return dto.toEntity(routeSearch);
    }

    private List<RouteOptionJsonDto> readAll() {
        return dbConnection.readAll(FILE_PATH, new TypeReference<List<RouteOptionJsonDto>>() {
        });
    }

    private long nextId(List<RouteOptionJsonDto> dtos) {
        return dtos.stream().mapToLong(RouteOptionJsonDto::id).max().orElse(0) + 1;
    }
}

package com.example.path.service;

import com.example.path.model.RouteOption;
import com.example.path.model.RouteType;
import com.example.path.repository.RouteOptionRepository;
import com.example.path.repository.RouteSearchRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class RouteOptionService {

    private final RouteOptionRepository repository;
    private final RouteSearchRepository routeSearchRepository;

    public RouteOptionService(
            RouteOptionRepository repository,
            RouteSearchRepository routeSearchRepository
    ) {
        this.repository = repository;
        this.routeSearchRepository = routeSearchRepository;
    }

    public RouteOption save(RouteOption routeOption) {
        validateRouteOption(routeOption);
        validateRouteSearchExists(routeOption);

        return repository.save(routeOption);
    }

    /**
     * 지도 API에서 받은 여러 경로를 한 번에 저장합니다.
     */
    public List<RouteOption> saveAll(List<RouteOption> routeOptions) {
        if (routeOptions == null || routeOptions.isEmpty()) {
            throw new IllegalArgumentException(
                    "저장할 경로 옵션이 필요합니다."
            );
        }

        for (RouteOption routeOption : routeOptions) {
            validateRouteOption(routeOption);
            validateRouteSearchExists(routeOption);
        }

        return repository.saveAll(routeOptions);
    }

    public List<RouteOption> findAll() {
        return repository.findAll();
    }

    public RouteOption findById(Long id) {
        validateId(id);

        RouteOption routeOption = repository.findById(id);

        if (routeOption == null) {
            throw new EntityNotFoundException(
                    "경로 옵션을 찾을 수 없습니다. id=" + id
            );
        }

        return routeOption;
    }

    /**
     * 특정 길찾기 요청에 포함된 추천·최단·최소시간 경로를 조회합니다.
     */
    public List<RouteOption> findByRouteSearchId(Long routeSearchId) {
        validateId(routeSearchId);

        if (routeSearchRepository.findById(routeSearchId) == null) {
            throw new EntityNotFoundException(
                    "길찾기 검색 기록을 찾을 수 없습니다. id="
                            + routeSearchId
            );
        }

        return repository.findByRouteSearchId(routeSearchId);
    }

    public List<RouteOption> findByRouteType(RouteType routeType) {
        Objects.requireNonNull(
                routeType,
                "경로 유형은 null일 수 없습니다."
        );

        return repository.findByRouteType(routeType);
    }

    public List<RouteOption> findByProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return List.of();
        }

        return repository.findByProvider(provider.trim());
    }

    /**
     * RouteSearch 연결, 옵션 순서, 경로 유형은 변경할 수 없습니다(옵션의 정체성).
     * 거리/소요시간/요금/폴리라인/제공자만 변경 가능합니다.
     */
    public RouteOption update(RouteOption routeOption) {
        validateRouteOption(routeOption);

        if (routeOption.getId() == null) {
            throw new IllegalArgumentException(
                    "수정할 경로 옵션의 ID가 필요합니다."
            );
        }

        RouteOption existing = findById(routeOption.getId());
        validateRouteSearchExists(routeOption);
        validateUnchangedIdentity(existing, routeOption);

        repository.update(routeOption);
        return routeOption;
    }

    public void delete(Long id) {
        findById(id);
        repository.delete(id);
    }

    /**
     * RouteSearch 삭제 전에 연결된 RouteOption을 삭제할 때 사용합니다.
     */
    public void deleteByRouteSearchId(Long routeSearchId) {
        validateId(routeSearchId);
        repository.deleteByRouteSearchId(routeSearchId);
    }

    private void validateUnchangedIdentity(RouteOption existing, RouteOption routeOption) {
        if (!existing.getRouteSearch().getId().equals(routeOption.getRouteSearch().getId())
                || !existing.getOptionNumber().equals(routeOption.getOptionNumber())
                || existing.getRouteType() != routeOption.getRouteType()) {
            throw new IllegalStateException(
                    "경로 옵션의 소속 검색/순서/유형은 변경할 수 없습니다. 삭제 후 다시 생성해주세요."
            );
        }
    }

    private void validateRouteOption(RouteOption routeOption) {
        Objects.requireNonNull(
                routeOption,
                "경로 옵션은 null일 수 없습니다."
        );

        Objects.requireNonNull(
                routeOption.getRouteSearch(),
                "경로 옵션에는 길찾기 검색 정보가 필요합니다."
        );

        if (routeOption.getOptionNumber() == null
                || routeOption.getOptionNumber() < 1) {
            throw new IllegalArgumentException(
                    "경로 옵션 순서는 1 이상이어야 합니다."
            );
        }

        Objects.requireNonNull(
                routeOption.getRouteType(),
                "경로 유형은 필수입니다."
        );

        if (routeOption.getDistanceMeters() == null
                || routeOption.getDistanceMeters() < 0) {
            throw new IllegalArgumentException(
                    "경로 거리는 0 이상이어야 합니다."
            );
        }

        if (routeOption.getDurationSeconds() == null
                || routeOption.getDurationSeconds() < 0) {
            throw new IllegalArgumentException(
                    "예상 소요시간은 0 이상이어야 합니다."
            );
        }

        if (routeOption.getProvider() == null
                || routeOption.getProvider().isBlank()) {
            throw new IllegalArgumentException(
                    "경로 제공자 정보는 필수입니다."
            );
        }
    }

    private void validateRouteSearchExists(RouteOption routeOption) {
        Long routeSearchId = routeOption.getRouteSearch().getId();

        if (routeSearchId == null) {
            throw new IllegalArgumentException(
                    "먼저 RouteSearch를 저장해야 합니다."
            );
        }

        if (routeSearchRepository.findById(routeSearchId) == null) {
            throw new EntityNotFoundException(
                    "연결할 길찾기 검색 기록을 찾을 수 없습니다. id="
                            + routeSearchId
            );
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "올바른 ID가 필요합니다."
            );
        }
    }
}

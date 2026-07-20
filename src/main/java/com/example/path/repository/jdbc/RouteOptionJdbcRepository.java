package com.example.path.repository.jdbc;

import com.example.path.model.RouteOption;
import com.example.path.model.RouteSearch;
import com.example.path.model.RouteType;
import com.example.path.repository.RouteOptionRepository;
import com.example.path.repository.RouteSearchRepository;
import com.example.path.util.DBConnection;
import com.example.path.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * route_option 테이블을 대상으로 순수 JDBC로 CRUD를 수행하는 RouteOptionRepository 구현체.
 * route_search_id(FK)는 JOIN 대신 RouteSearchRepository.findById(...)로 해석해
 * JSON 구현체와 구조를 대칭적으로 유지한다.
 */
public class RouteOptionJdbcRepository implements RouteOptionRepository {

    private static final String INSERT = """
            INSERT INTO route_option
              (route_search_id, option_number, route_type, distance_meters,
               duration_seconds, estimated_fare, polyline, provider)
            VALUES (?,?,?,?,?,?,?,?)""";

    private static final String SELECT_ALL = "SELECT * FROM route_option";
    private static final String SELECT_BY_ID = "SELECT * FROM route_option WHERE id = ?";
    private static final String UPDATE = """
            UPDATE route_option SET
              route_search_id = ?, option_number = ?, route_type = ?, distance_meters = ?,
              duration_seconds = ?, estimated_fare = ?, polyline = ?, provider = ?
            WHERE id = ?""";
    private static final String DELETE = "DELETE FROM route_option WHERE id = ?";
    private static final String SELECT_BY_ROUTE_SEARCH_ID = "SELECT * FROM route_option WHERE route_search_id = ?";
    private static final String SELECT_BY_ROUTE_TYPE = "SELECT * FROM route_option WHERE route_type = ?";
    private static final String SELECT_BY_PROVIDER = "SELECT * FROM route_option WHERE LOWER(provider) = LOWER(?)";
    private static final String DELETE_BY_ROUTE_SEARCH_ID = "DELETE FROM route_option WHERE route_search_id = ?";

    private final DBConnection connection;
    private final RouteSearchRepository routeSearchRepository;

    public RouteOptionJdbcRepository(DBConnection connection, RouteSearchRepository routeSearchRepository) {
        this.connection = connection;
        this.routeSearchRepository = routeSearchRepository;
    }

    @Override
    public RouteOption save(RouteOption routeOption) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, routeOption);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return routeOption.toBuilder().id(keys.getLong(1)).build();
            }
        } catch (SQLException e) {
            throw new DataAccessException("경로 옵션 저장 실패", e);
        }
    }

    @Override
    public List<RouteOption> saveAll(List<RouteOption> routeOptions) {
        List<RouteOption> saved = new ArrayList<>();
        for (RouteOption routeOption : routeOptions) {
            saved.add(save(routeOption));
        }
        return saved;
    }

    @Override
    public List<RouteOption> findAll() {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        } catch (SQLException e) {
            throw new DataAccessException("경로 옵션 전체 조회 실패", e);
        }
    }

    @Override
    public RouteOption findById(Long id) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("경로 옵션 조회 실패", e);
        }
    }

    @Override
    public void update(RouteOption routeOption) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE)) {
            int index = bindParams(ps, routeOption);
            ps.setLong(index, routeOption.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("경로 옵션 수정 실패", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("경로 옵션 삭제 실패", e);
        }
    }

    @Override
    public List<RouteOption> findByRouteSearchId(Long routeSearchId) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ROUTE_SEARCH_ID)) {
            ps.setLong(1, routeSearchId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("검색별 경로 옵션 조회 실패", e);
        }
    }

    @Override
    public List<RouteOption> findByRouteType(RouteType routeType) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ROUTE_TYPE)) {
            ps.setString(1, routeType.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("유형별 경로 옵션 조회 실패", e);
        }
    }

    @Override
    public List<RouteOption> findByProvider(String provider) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_PROVIDER)) {
            ps.setString(1, provider);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("제공자별 경로 옵션 조회 실패", e);
        }
    }

    @Override
    public void deleteByRouteSearchId(Long routeSearchId) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_BY_ROUTE_SEARCH_ID)) {
            ps.setLong(1, routeSearchId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("검색별 경로 옵션 삭제 실패", e);
        }
    }

    private int bindParams(PreparedStatement ps, RouteOption routeOption) throws SQLException {
        int i = 1;
        ps.setLong(i++, routeOption.getRouteSearch().getId());
        ps.setInt(i++, routeOption.getOptionNumber());
        ps.setString(i++, routeOption.getRouteType().name());
        ps.setInt(i++, routeOption.getDistanceMeters());
        ps.setInt(i++, routeOption.getDurationSeconds());
        if (routeOption.getEstimatedFare() != null) {
            ps.setInt(i++, routeOption.getEstimatedFare());
        } else {
            ps.setNull(i++, Types.INTEGER);
        }
        ps.setString(i++, routeOption.getPolyline());
        ps.setString(i++, routeOption.getProvider());
        return i;
    }

    private List<RouteOption> mapRows(ResultSet rs) throws SQLException {
        List<RouteOption> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs));
        }
        return result;
    }

    private RouteOption mapRow(ResultSet rs) throws SQLException {
        RouteSearch routeSearch = routeSearchRepository.findById(rs.getLong("route_search_id"));
        Integer estimatedFare = rs.getObject("estimated_fare", Integer.class);

        return RouteOption.builder()
                .id(rs.getLong("id"))
                .routeSearch(routeSearch)
                .optionNumber(rs.getInt("option_number"))
                .routeType(RouteType.valueOf(rs.getString("route_type")))
                .distanceMeters(rs.getInt("distance_meters"))
                .durationSeconds(rs.getInt("duration_seconds"))
                .estimatedFare(estimatedFare)
                .polyline(rs.getString("polyline"))
                .provider(rs.getString("provider"))
                .build();
    }
}

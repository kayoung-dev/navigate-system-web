package com.example.path.repository.jdbc;

import com.example.path.model.Location;
import com.example.path.model.RouteSearch;
import com.example.path.model.TransportMode;
import com.example.path.model.TravelPurpose;
import com.example.path.repository.RouteSearchRepository;
import com.example.path.util.DBConnection;
import com.example.path.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * route_search 테이블을 대상으로 순수 JDBC로 CRUD를 수행하는 RouteSearchRepository 구현체.
 */
public class RouteSearchJdbcRepository implements RouteSearchRepository {

    private static final String INSERT = """
            INSERT INTO route_search
              (start_name, start_address, start_latitude, start_longitude,
               destination_name, destination_address, destination_latitude, destination_longitude,
               transport_mode, purpose, searched_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)""";

    private static final String SELECT_ALL = "SELECT * FROM route_search";
    private static final String SELECT_BY_ID = "SELECT * FROM route_search WHERE id = ?";
    private static final String UPDATE = """
            UPDATE route_search SET
              start_name = ?, start_address = ?, start_latitude = ?, start_longitude = ?,
              destination_name = ?, destination_address = ?, destination_latitude = ?, destination_longitude = ?,
              transport_mode = ?, purpose = ?, searched_at = ?
            WHERE id = ?""";
    private static final String DELETE = "DELETE FROM route_search WHERE id = ?";
    private static final String SELECT_BY_KEYWORD = """
            SELECT * FROM route_search WHERE
              LOWER(start_name) LIKE LOWER(?) OR LOWER(start_address) LIKE LOWER(?)
              OR LOWER(destination_name) LIKE LOWER(?) OR LOWER(destination_address) LIKE LOWER(?)""";
    private static final String SELECT_BY_PURPOSE = "SELECT * FROM route_search WHERE purpose = ?";
    private static final String SELECT_BY_TRANSPORT_MODE = "SELECT * FROM route_search WHERE transport_mode = ?";
    private static final String SELECT_BY_SEARCHED_AT_BETWEEN =
            "SELECT * FROM route_search WHERE searched_at BETWEEN ? AND ?";

    private final DBConnection connection;

    public RouteSearchJdbcRepository(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public RouteSearch save(RouteSearch routeSearch) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, routeSearch);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return routeSearch.toBuilder().id(keys.getLong(1)).build();
            }
        } catch (SQLException e) {
            throw new DataAccessException("길찾기 검색 저장 실패", e);
        }
    }

    @Override
    public List<RouteSearch> findAll() {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        } catch (SQLException e) {
            throw new DataAccessException("길찾기 검색 전체 조회 실패", e);
        }
    }

    @Override
    public RouteSearch findById(Long id) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("길찾기 검색 조회 실패", e);
        }
    }

    @Override
    public void update(RouteSearch routeSearch) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE)) {
            int index = bindParams(ps, routeSearch);
            ps.setLong(index, routeSearch.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("길찾기 검색 수정 실패", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("길찾기 검색 삭제 실패", e);
        }
    }

    @Override
    public List<RouteSearch> findByKeyword(String keyword) {
        String pattern = "%" + keyword + "%";
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_KEYWORD)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("키워드 검색 실패", e);
        }
    }

    @Override
    public List<RouteSearch> findByPurpose(TravelPurpose purpose) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_PURPOSE)) {
            ps.setString(1, purpose.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("목적별 검색 실패", e);
        }
    }

    @Override
    public List<RouteSearch> findByTransportMode(TransportMode transportMode) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_TRANSPORT_MODE)) {
            ps.setString(1, transportMode.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("이동수단별 검색 실패", e);
        }
    }

    @Override
    public List<RouteSearch> findBySearchedAtBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        try (Connection c = connection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_SEARCHED_AT_BETWEEN)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDateTime));
            ps.setTimestamp(2, Timestamp.valueOf(endDateTime));
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("기간별 검색 실패", e);
        }
    }

    private int bindParams(PreparedStatement ps, RouteSearch routeSearch) throws SQLException {
        Location start = routeSearch.getStartLocation();
        Location destination = routeSearch.getDestinationLocation();

        int i = 1;
        ps.setString(i++, start.getName());
        ps.setString(i++, start.getAddress());
        ps.setDouble(i++, start.getLatitude());
        ps.setDouble(i++, start.getLongitude());
        ps.setString(i++, destination.getName());
        ps.setString(i++, destination.getAddress());
        ps.setDouble(i++, destination.getLatitude());
        ps.setDouble(i++, destination.getLongitude());
        ps.setString(i++, routeSearch.getTransportMode().name());
        ps.setString(i++, routeSearch.getPurpose().name());
        ps.setTimestamp(i++, Timestamp.valueOf(routeSearch.getSearchedAt()));
        return i;
    }

    private List<RouteSearch> mapRows(ResultSet rs) throws SQLException {
        List<RouteSearch> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs));
        }
        return result;
    }

    private RouteSearch mapRow(ResultSet rs) throws SQLException {
        Location start = new Location(
                rs.getString("start_name"),
                rs.getString("start_address"),
                rs.getDouble("start_latitude"),
                rs.getDouble("start_longitude")
        );
        Location destination = new Location(
                rs.getString("destination_name"),
                rs.getString("destination_address"),
                rs.getDouble("destination_latitude"),
                rs.getDouble("destination_longitude")
        );

        return RouteSearch.builder()
                .id(rs.getLong("id"))
                .startLocation(start)
                .destinationLocation(destination)
                .transportMode(TransportMode.valueOf(rs.getString("transport_mode")))
                .purpose(TravelPurpose.valueOf(rs.getString("purpose")))
                .searchedAt(rs.getTimestamp("searched_at").toLocalDateTime())
                .build();
    }
}

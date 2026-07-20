package com.example.path.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * db.properties를 읽어 MariaDB에 연결하는 DataSource(HikariCP)를 관리하는 싱글턴.
 */
public class DBConnection {

    private static final DBConnection INSTANCE = new DBConnection();

    private final HikariDataSource dataSource;

    private DBConnection() {
        Properties props = loadProperties();

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(props.getProperty("db.driver"));
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(10);

        this.dataSource = new HikariDataSource(config);
        Runtime.getRuntime().addShutdownHook(new Thread(dataSource::close));
    }

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties를 classpath에서 찾을 수 없습니다.");
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new DataAccessException("db.properties 로드 실패", e);
        }
    }
}

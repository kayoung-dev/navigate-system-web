package com.example.path;

import com.example.path.repository.RouteOptionRepository;
import com.example.path.repository.RouteSearchRepository;
import com.example.path.repository.jdbc.RouteOptionJdbcRepository;
import com.example.path.repository.jdbc.RouteSearchJdbcRepository;
import com.example.path.repository.json.RouteOptionJsonRepository;
import com.example.path.repository.json.RouteSearchJsonRepository;
import com.example.path.service.RouteOptionService;
import com.example.path.service.RouteSearchService;
import com.example.path.util.DBConnection;
import com.example.path.util.JsonFileConnection;
import com.example.path.view.ConsoleView;

public class Main {

    public static void main(String[] args) {
        DBConnection connection = DBConnection.getInstance();

        // 저장 방식은 인터페이스 타입 변수에 구현체를 대입하는 한 줄만 바꾸면 교체할 수 있다.
        RouteSearchRepository routeSearchRepository = new RouteSearchJdbcRepository(connection); // DB(JDBC) 버전
        // RouteSearchRepository routeSearchRepository =
        //         new RouteSearchJsonRepository(JsonFileConnection.getInstance()); // 파일(JSON) 버전

        RouteOptionRepository routeOptionRepository =
                new RouteOptionJdbcRepository(connection, routeSearchRepository); // DB(JDBC) 버전
        // RouteOptionRepository routeOptionRepository =
        //         new RouteOptionJsonRepository(JsonFileConnection.getInstance(), routeSearchRepository); // 파일(JSON) 버전

        RouteOptionService routeOptionService = new RouteOptionService(routeOptionRepository, routeSearchRepository);
        RouteSearchService routeSearchService = new RouteSearchService(routeSearchRepository, routeOptionService);

        ConsoleView consoleView = new ConsoleView(routeSearchService, routeOptionService);
        consoleView.run();
    }
}

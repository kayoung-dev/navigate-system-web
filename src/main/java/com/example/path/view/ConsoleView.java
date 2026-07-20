package com.example.path.view;

import com.example.path.model.Location;
import com.example.path.model.RouteOption;
import com.example.path.model.RouteSearch;
import com.example.path.model.RouteType;
import com.example.path.model.TransportMode;
import com.example.path.model.TravelPurpose;
import com.example.path.service.RouteOptionService;
import com.example.path.service.RouteSearchService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 길찾기 CRUD 콘솔 메뉴. 사용자 입출력만 담당하고, 판단/검증은 service 계층에 위임한다.
 */
public class ConsoleView {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RouteSearchService routeSearchService;
    private final RouteOptionService routeOptionService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleView(RouteSearchService routeSearchService, RouteOptionService routeOptionService) {
        this.routeSearchService = routeSearchService;
        this.routeOptionService = routeOptionService;
    }

    public void run() {
        System.out.println("안녕하세요, 길찾기 CRUD 시스템입니다~");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("=> 원하는 메뉴는? ");

            try {
                switch (choice) {
                    case 1 -> handleFindAll();
                    case 2 -> handleCreate();
                    case 3 -> handleUpdateRouteSearch();
                    case 4 -> handleUpdateRouteOption();
                    case 5 -> handleDelete();
                    case 6 -> handleSearch();
                    case 7 -> handleRouteOptionSearch();
                    case 0 -> {
                        running = false;
                        System.out.println("길찾기 CRUD 시스템을 종료합니다.");
                    }
                    default -> System.out.println("=> 올바른 메뉴 번호를 입력해주세요.");
                }
            } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
                System.out.println("=> " + e.getMessage());
            }
        }
    }

    private void printMainMenu() {
        System.out.println("-------------- 길찾기 CRUD System --------------");
        System.out.println("1. 검색 기록 전체 조회");
        System.out.println("2. 길찾기 검색 등록");
        System.out.println("3. 검색 조건 변경");
        System.out.println("4. 경로 옵션 변경");
        System.out.println("5. 검색 기록 삭제");
        System.out.println("6. 조건별 검색");
        System.out.println("7. 경로 옵션 조회");
        System.out.println("0. 종료");
        System.out.println("-------------------------------------------------");
    }

    // 1. 검색 기록 전체 조회
    private void handleFindAll() {
        List<RouteSearch> all = routeSearchService.findAll();
        printRouteSearchTable(all);

        if (all.isEmpty()) {
            return;
        }

        long id = readLong("상세 조회할 ID는? (건너뛰려면 0) ");
        if (id == 0) {
            return;
        }

        RouteSearch routeSearch = routeSearchService.findById(id);
        List<RouteOption> options = routeOptionService.findByRouteSearchId(id);

        System.out.println("[검색 기록 상세] id=" + routeSearch.getId());
        printRouteSearchTable(List.of(routeSearch));
        System.out.println("-- 연결된 경로 옵션 --");
        printRouteOptionTable(options);
    }

    // 2. 길찾기 검색 등록
    private void handleCreate() {
        System.out.println("-- 길찾기 검색 등록 --");
        Location start = readLocation("출발지");
        Location destination = readLocation("도착지");
        TransportMode transportMode = promptTransportMode();
        TravelPurpose purpose = promptTravelPurpose();

        RouteSearch routeSearch = RouteSearch.builder()
                .startLocation(start)
                .destinationLocation(destination)
                .transportMode(transportMode)
                .purpose(purpose)
                .build();

        RouteSearch saved = routeSearchService.save(routeSearch);
        System.out.println("=> 검색 기록이 등록되었습니다. id=" + saved.getId());

        List<RouteOption> options = new ArrayList<>();
        int optionNumber = 1;
        while (readInt("경로 옵션을 추가하시겠습니까? (예:1, 아니오:0) ") == 1) {
            RouteType routeType = promptRouteType();
            int distance = readInt("거리(m)는? ");
            int duration = readInt("소요시간(초)는? ");
            Integer fare = readOptionalInt("예상 요금은? (없으면 빈칸) ");
            String polyline = readOptionalLine("폴리라인은? (없으면 빈칸) ");
            String provider = readLine("제공자는? ");

            options.add(RouteOption.builder()
                    .routeSearch(saved)
                    .optionNumber(optionNumber++)
                    .routeType(routeType)
                    .distanceMeters(distance)
                    .durationSeconds(duration)
                    .estimatedFare(fare)
                    .polyline(polyline)
                    .provider(provider)
                    .build());
        }

        if (!options.isEmpty()) {
            routeOptionService.saveAll(options);
            System.out.println("=> 경로 옵션 " + options.size() + "건이 추가되었습니다.");
        }
    }

    // 3. 검색 조건 변경 (출발지/도착지 변경 불가 — 이동수단/목적만 변경 가능)
    private void handleUpdateRouteSearch() {
        printRouteSearchTable(routeSearchService.findAll());
        long id = readLong("변경할 ID는? ");
        RouteSearch existing = routeSearchService.findById(id);

        System.out.println("현재 이동수단: " + existing.getTransportMode() + ", 목적: " + existing.getPurpose());
        System.out.println("(출발지/도착지는 변경할 수 없습니다. 새로운 검색으로 등록해주세요.)");

        TransportMode transportMode = promptTransportMode();
        TravelPurpose purpose = promptTravelPurpose();

        RouteSearch updated = existing.toBuilder()
                .transportMode(transportMode)
                .purpose(purpose)
                .build();

        routeSearchService.update(updated);
        System.out.println("=> 검색 조건이 변경되었습니다.");
    }

    // 4. 경로 옵션 변경 (소속 검색/순서/유형 변경 불가 — 거리/시간/요금/폴리라인/제공자만 변경 가능)
    private void handleUpdateRouteOption() {
        printRouteOptionTable(routeOptionService.findAll());
        long id = readLong("변경할 옵션 ID는? ");
        RouteOption existing = routeOptionService.findById(id);

        System.out.println("(소속 검색/순서/유형은 변경할 수 없습니다. 삭제 후 다시 생성해주세요.)");

        int distance = readIntOrKeep("거리(m)는? (현재 " + existing.getDistanceMeters() + ", 유지하려면 빈칸) ",
                existing.getDistanceMeters());
        int duration = readIntOrKeep("소요시간(초)는? (현재 " + existing.getDurationSeconds() + ", 유지하려면 빈칸) ",
                existing.getDurationSeconds());
        Integer fare = readOptionalIntOrKeep("예상 요금은? (현재 " + existing.getEstimatedFare() + ", 유지하려면 빈칸) ",
                existing.getEstimatedFare());
        String polyline = readLineOrKeep("폴리라인은? (유지하려면 빈칸) ", existing.getPolyline());
        String provider = readLineOrKeep("제공자는? (현재 " + existing.getProvider() + ", 유지하려면 빈칸) ",
                existing.getProvider());

        RouteOption updated = existing.toBuilder()
                .distanceMeters(distance)
                .durationSeconds(duration)
                .estimatedFare(fare)
                .polyline(polyline)
                .provider(provider)
                .build();

        routeOptionService.update(updated);
        System.out.println("=> 경로 옵션이 변경되었습니다.");
    }

    // 5. 검색 기록 삭제 (연관 경로 옵션 cascade 삭제 + 2단계 확인)
    private void handleDelete() {
        printRouteSearchTable(routeSearchService.findAll());
        long id = readLong("삭제할 번호는 (취소:0)? ");
        if (id == 0) {
            System.out.println("=> 삭제가 취소되었습니다.");
            return;
        }

        routeSearchService.findById(id);
        int confirm = readInt("정말로 삭제하시겠습니까 (삭제:1)? ");
        if (confirm != 1) {
            System.out.println("=> 삭제가 취소되었습니다.");
            return;
        }

        routeSearchService.delete(id);
        System.out.println("=> 삭제되었습니다.");
    }

    // 6. 조건별 검색
    private void handleSearch() {
        System.out.println("1. 키워드로 검색  2. 목적으로 검색  3. 이동수단으로 검색  4. 기간으로 검색");
        int choice = readInt("=> 원하는 메뉴는? ");

        List<RouteSearch> result = switch (choice) {
            case 1 -> routeSearchService.findByKeyword(readLine("검색할 키워드는? "));
            case 2 -> routeSearchService.findByPurpose(promptTravelPurpose());
            case 3 -> routeSearchService.findByTransportMode(promptTransportMode());
            case 4 -> {
                LocalDateTime start = readDateTime("검색 시작 일시는? (yyyy-MM-dd HH:mm) ");
                LocalDateTime end = readDateTime("검색 종료 일시는? (yyyy-MM-dd HH:mm) ");
                yield routeSearchService.findByPeriod(start, end);
            }
            default -> List.of();
        };

        printRouteSearchTable(result);
    }

    // 7. 경로 옵션 조회
    private void handleRouteOptionSearch() {
        System.out.println("1. 경로 유형으로 조회  2. 제공자로 조회");
        int choice = readInt("=> 원하는 메뉴는? ");

        List<RouteOption> result = switch (choice) {
            case 1 -> routeOptionService.findByRouteType(promptRouteType());
            case 2 -> routeOptionService.findByProvider(readLine("검색할 제공자는? "));
            default -> List.of();
        };

        printRouteOptionTable(result);
    }

    private void printRouteSearchTable(List<RouteSearch> list) {
        if (list.isEmpty()) {
            System.out.println("표시할 검색 기록이 없습니다.");
            return;
        }

        System.out.printf("%-4s%-6s%-14s%-14s%-16s%-10s%-18s%n",
                "No", "ID", "출발", "도착", "이동수단", "목적", "검색시각");

        int no = 1;
        for (RouteSearch routeSearch : list) {
            System.out.printf("%-4d%-6d%-14s%-14s%-16s%-10s%-18s%n",
                    no++,
                    routeSearch.getId(),
                    routeSearch.getStartLocation().getName(),
                    routeSearch.getDestinationLocation().getName(),
                    routeSearch.getTransportMode(),
                    routeSearch.getPurpose(),
                    DATE_FORMATTER.format(routeSearch.getSearchedAt()));
        }
    }

    private void printRouteOptionTable(List<RouteOption> list) {
        if (list.isEmpty()) {
            System.out.println("표시할 경로 옵션이 없습니다.");
            return;
        }

        System.out.printf("%-4s%-6s%-8s%-14s%-10s%-10s%-10s%-12s%n",
                "No", "ID", "순번", "유형", "거리(m)", "시간(초)", "요금", "제공자");

        int no = 1;
        for (RouteOption routeOption : list) {
            System.out.printf("%-4d%-6d%-8d%-14s%-10d%-10d%-10s%-12s%n",
                    no++,
                    routeOption.getId(),
                    routeOption.getOptionNumber(),
                    routeOption.getRouteType(),
                    routeOption.getDistanceMeters(),
                    routeOption.getDurationSeconds(),
                    routeOption.getEstimatedFare() == null ? "-" : routeOption.getEstimatedFare(),
                    routeOption.getProvider());
        }
    }

    private Location readLocation(String label) {
        System.out.println("[" + label + "]");
        String name = readLine("이름은? ");
        String address = readLine("주소는? ");
        double latitude = readDouble("위도는? ");
        double longitude = readDouble("경도는? ");
        return new Location(name, address, latitude, longitude);
    }

    private TransportMode promptTransportMode() {
        return promptEnum("이동수단을 선택하세요.", TransportMode.values());
    }

    private TravelPurpose promptTravelPurpose() {
        return promptEnum("이동 목적을 선택하세요.", TravelPurpose.values());
    }

    private RouteType promptRouteType() {
        return promptEnum("경로 유형을 선택하세요.", RouteType.values());
    }

    private <E extends Enum<E>> E promptEnum(String title, E[] values) {
        System.out.println(title);
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i]);
        }

        while (true) {
            int choice = readInt("선택: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("=> 목록에 있는 번호를 입력해주세요.");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readOptionalLine(String prompt) {
        String value = readLine(prompt);
        return value.isBlank() ? null : value;
    }

    private String readLineOrKeep(String prompt, String current) {
        String value = readLine(prompt);
        return value.isBlank() ? current : value;
    }

    private int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("=> 숫자를 입력해주세요.");
            }
        }
    }

    private int readIntOrKeep(String prompt, int current) {
        String value = readLine(prompt);
        if (value.isBlank()) {
            return current;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("=> 숫자를 입력해주세요. 기존 값을 유지합니다.");
            return current;
        }
    }

    private Integer readOptionalInt(String prompt) {
        String value = readLine(prompt);
        if (value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("=> 숫자를 입력해주세요. 값을 비워둡니다.");
            return null;
        }
    }

    private Integer readOptionalIntOrKeep(String prompt, Integer current) {
        String value = readLine(prompt);
        if (value.isBlank()) {
            return current;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("=> 숫자를 입력해주세요. 기존 값을 유지합니다.");
            return current;
        }
    }

    private long readLong(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.out.println("=> 숫자를 입력해주세요.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("=> 숫자를 입력해주세요.");
            }
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return LocalDateTime.parse(value, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("=> yyyy-MM-dd HH:mm 형식으로 입력해주세요.");
            }
        }
    }
}

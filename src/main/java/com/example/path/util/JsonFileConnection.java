package com.example.path.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * route_search.json / route_option.json 파일에 대한 읽기·쓰기를 담당하는 싱글턴.
 * 파일(JSON) 버전 저장소가 사용하는 접근 계층으로, 실제 DB 연결(DBConnection)과는 별개다.
 */
public class JsonFileConnection {

    private static final JsonFileConnection INSTANCE = new JsonFileConnection();

    private final ObjectMapper objectMapper;

    private JsonFileConnection() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static JsonFileConnection getInstance() {
        return INSTANCE;
    }

    public synchronized <T> List<T> readAll(Path filePath, TypeReference<List<T>> typeReference) {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<T> data = objectMapper.readValue(filePath.toFile(), typeReference);
            return data == null ? new ArrayList<>() : new ArrayList<>(data);
        } catch (IOException e) {
            throw new UncheckedIOException("파일을 읽는 중 오류가 발생했습니다. path=" + filePath, e);
        }
    }

    public synchronized <T> void writeAll(Path filePath, List<T> data) {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writeValue(filePath.toFile(), data);
        } catch (IOException e) {
            throw new UncheckedIOException("파일을 쓰는 중 오류가 발생했습니다. path=" + filePath, e);
        }
    }
}

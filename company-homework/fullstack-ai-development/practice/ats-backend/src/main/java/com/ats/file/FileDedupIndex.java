package com.ats.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地文件内容去重索引：SHA-256 → 对外 URL。
 * MVP 用 JSON 文件持久化在 upload 根目录，生产可换 Redis / DB。
 */
@Slf4j
@Component
public class FileDedupIndex {

    private final Path indexFile;
    private final ObjectMapper objectMapper;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public FileDedupIndex(UploadProperties props, ObjectMapper objectMapper) {
        this.indexFile = Path.of(props.getPath()).toAbsolutePath().normalize().resolve(".dedup-index.json");
        this.objectMapper = objectMapper;
        load();
    }

    public String findUrl(String sha256) {
        return cache.get(sha256);
    }

    public void put(String sha256, String url) {
        cache.put(sha256, url);
        persist();
    }

    private void load() {
        if (!Files.exists(indexFile)) {
            return;
        }
        try {
            Map<String, String> map = objectMapper.readValue(Files.readString(indexFile), new TypeReference<>() {});
            cache.putAll(map);
        }
        catch (IOException e) {
            log.warn("[FILE] dedup index load failed: {}", e.toString());
        }
    }

    private void persist() {
        try {
            Files.writeString(indexFile, objectMapper.writeValueAsString(cache));
        }
        catch (IOException e) {
            log.warn("[FILE] dedup index persist failed: {}", e.toString());
        }
    }
}

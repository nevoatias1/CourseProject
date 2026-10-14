package com.example.store.repository;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

final class GeneratedKeySupport {
    private GeneratedKeySupport() { }

    static int requiredId(KeyHolder keyHolder) {
        Object id = keyHolder.getKeys().entrySet().stream()
                .filter(entry -> "id".equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new InvalidDataAccessApiUsageException("Generated key column 'id' was not returned"));

        if (!(id instanceof Number number)) {
            throw new InvalidDataAccessApiUsageException("Generated key column 'id' is not numeric");
        }
        return number.intValue();
    }
}

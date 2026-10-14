package com.example.store.repository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneratedKeySupportTests {
    @Test
    void readsIdWhenDriverReturnsMultipleGeneratedColumns() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder(List.of(Map.of(
                "id", 42,
                "username", "checkout_user",
                "status", "PLACED"
        )));

        assertThrows(InvalidDataAccessApiUsageException.class, keyHolder::getKey);
        assertEquals(42, GeneratedKeySupport.requiredId(keyHolder));
    }
}

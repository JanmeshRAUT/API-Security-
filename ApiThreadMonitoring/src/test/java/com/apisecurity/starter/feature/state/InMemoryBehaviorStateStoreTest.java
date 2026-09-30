package com.apisecurity.starter.feature.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryBehaviorStateStoreTest {

    private InMemoryBehaviorStateStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryBehaviorStateStore();
    }

    @Test
    void testIncrementAndGetRequestCount() {
        assertEquals(1, store.incrementAndGetRequestCount("key1"));
        assertEquals(2, store.incrementAndGetRequestCount("key1"));
        assertEquals(1, store.incrementAndGetRequestCount("key2"));
    }

    @Test
    void testIncrementAndGetFailedCount() {
        assertEquals(1, store.incrementAndGetFailedCount("key1"));
        assertEquals(2, store.incrementAndGetFailedCount("key1"));
    }

    @Test
    void testAddToUniqueSetAndGetSize() {
        assertEquals(1, store.addToUniqueSetAndGetSize("key1", "user1"));
        assertEquals(2, store.addToUniqueSetAndGetSize("key1", "user2"));
        assertEquals(2, store.addToUniqueSetAndGetSize("key1", "user2")); // Duplicate
        assertEquals(2, store.getUniqueSetSize("key1"));
        assertEquals(0, store.getUniqueSetSize("unknown"));
    }
}

package com.team766.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.team766.TestCase;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class StatusBusTest extends TestCase {

    public record MyStatus(int value) implements Status {}

    public record OtherStatus(int value) implements Status {}

    public StatusBus statusBus = StatusBus.getInstance();

    @Test
    public void testGetStatusEntry() {
        assertEquals(Optional.empty(), statusBus.getStatusEntry(MyStatus.class));
        statusBus.publishStatus(new OtherStatus(0));
        assertEquals(Optional.empty(), statusBus.getStatusEntry(MyStatus.class));

        testClock.setTime(1234500000);
        statusBus.publishStatus(new MyStatus(0));

        testClock.setTime(1234500123);
        var maybeEntry = statusBus.getStatusEntry(MyStatus.class);
        var entry = maybeEntry.orElseThrow();
        assertEquals(new MyStatus(0), entry.status());
        assertEquals(1234500000, entry.timestamp());
        assertEquals(123, entry.age());
    }

    @Test
    public void testPublishStatus() {
        testClock.setTime(1234500000);
        var publishEntry = statusBus.publishStatus(new MyStatus(42));
        assertEquals(new MyStatus(42), publishEntry.status());
        assertEquals(1234500000, publishEntry.timestamp());

        testClock.setTime(1234500123);
        assertEquals(123, publishEntry.age());

        var maybeEntry = statusBus.getStatusEntry(MyStatus.class);
        var entry = maybeEntry.orElseThrow();
        assertEquals(new MyStatus(42), entry.status());
        assertEquals(1234500000, entry.timestamp());
        assertEquals(123, entry.age());

        // Test that publishing another status overwrites the first status.

        testClock.setTime(1234501000);
        publishEntry = statusBus.publishStatus(new MyStatus(66));
        assertEquals(new MyStatus(66), publishEntry.status());
        assertEquals(1234501000, publishEntry.timestamp());

        testClock.setTime(1234501012);
        assertEquals(12, publishEntry.age());

        entry = statusBus.getStatusEntry(MyStatus.class).orElseThrow();
        assertEquals(new MyStatus(66), entry.status());
        assertEquals(1234501000, entry.timestamp());
        assertEquals(12, entry.age());
    }

    @Test
    public void testClear() {
        statusBus.publishStatus(new MyStatus(0));
        assertTrue(statusBus.getStatusEntry(MyStatus.class).isPresent());
        statusBus.clear();
        assertFalse(statusBus.getStatusEntry(MyStatus.class).isPresent());
    }

    @Test
    public void testGetStatus() {
        assertEquals(Optional.empty(), statusBus.getStatus(MyStatus.class));
        statusBus.publishStatus(new OtherStatus(0));
        assertEquals(Optional.empty(), statusBus.getStatus(MyStatus.class));
        statusBus.publishStatus(new MyStatus(0));
        assertEquals(Optional.of(new MyStatus(0)), statusBus.getStatus(MyStatus.class));
    }

    @Test
    public void testGetStatusOrThrow() {
        assertThrows(
                NoSuchElementException.class, () -> statusBus.getStatusOrThrow(MyStatus.class));
        statusBus.publishStatus(new OtherStatus(0));
        assertThrows(
                NoSuchElementException.class, () -> statusBus.getStatusOrThrow(MyStatus.class));
        statusBus.publishStatus(new MyStatus(0));
        assertEquals(new MyStatus(0), statusBus.getStatusOrThrow(MyStatus.class));
    }

    @Test
    public void testGetStatusValue() {
        assertEquals(Optional.empty(), statusBus.getStatusValue(MyStatus.class, s -> s.value()));
        statusBus.publishStatus(new OtherStatus(0));
        assertEquals(Optional.empty(), statusBus.getStatusValue(MyStatus.class, s -> s.value()));
        statusBus.publishStatus(new MyStatus(42));
        assertEquals(Optional.of(42), statusBus.getStatusValue(MyStatus.class, s -> s.value()));
    }
}

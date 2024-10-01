package com.team766.framework3;

import java.util.Optional;
import java.util.function.Function;

public interface StatusBusMixin {
    /**
     * @see StatusBus#publishStatus(Record)
     */
    default <S extends Record & Status> StatusBus.Entry<S> publishStatus(S status) {
        return StatusBus.getInstance().publishStatus(status);
    }

    /**
     * @see StatusBus#getStatusEntry(Class)
     */
    default <S extends Status> Optional<StatusBus.Entry<S>> getStatusEntry(Class<S> statusClass) {
        return StatusBus.getInstance().getStatusEntry(statusClass);
    }

    /**
     * @see StatusBus#getStatus(Class)
     */
    default <S extends Status> Optional<S> getStatus(Class<S> statusClass) {
        return StatusBus.getInstance().getStatus(statusClass);
    }

    /**
     * @see StatusBus#getStatusOrThrow(Class)
     */
    default <S extends Status> S getStatusOrThrow(Class<S> statusClass) {
        return StatusBus.getInstance().getStatusOrThrow(statusClass);
    }

    /**
     * @see StatusBus#getStatusValue(Class, Function)
     */
    default <S extends Status, V> Optional<V> getStatusValue(
            Class<S> statusClass, Function<S, V> getter) {
        return StatusBus.getInstance().getStatusValue(statusClass, getter);
    }
}

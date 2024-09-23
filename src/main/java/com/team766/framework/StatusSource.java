package com.team766.framework;

public interface StatusSource {
    boolean isStatusActive();

    /**
     * Alias for {@link StatusBus#publishStatus(Record)}
     */
    default <S extends Record & Status> StatusBus.Entry<S> publishStatus(S status) {
        return StatusBus.getInstance().publishStatus(status, this);
    }
}

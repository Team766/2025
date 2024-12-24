package com.team766.framework3;

import com.team766.library.ReflectionUtils;

public interface StatusSource<S extends Record & Status> extends StatusHandle<S> {
    default S getStatus() {
        return StatusBus.getInstance().getStatusOrThrow(ReflectionUtils.getClass(this));
    }

    default void publishStatus(S status) {
        StatusBus.getInstance().publishStatus(ReflectionUtils.getClass(this), status);
    }
}

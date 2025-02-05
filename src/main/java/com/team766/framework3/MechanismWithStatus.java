package com.team766.framework3;

import com.team766.library.RateLimiter;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class MechanismWithStatus<S extends Record & Status> extends Mechanism
        implements StatusSource<S> {
    private final RateLimiter rateLimiter = new RateLimiter(1.0);
    private S status = null;

    protected final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    @Override
    /* package */ final void publishStatus() {
        S newStatus = updateStatus();
        // Only publish the status if it has changed or if enough time has elapsed since the last
        // publish
        if (rateLimiter.next() || !Objects.equals(status, newStatus)) {
            StatusBus.getInstance().publishStatus(newStatus);
        }
        status = newStatus;
    }

    protected abstract S updateStatus();
}

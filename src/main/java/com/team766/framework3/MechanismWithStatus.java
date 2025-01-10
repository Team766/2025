package com.team766.framework3;

import java.util.NoSuchElementException;

public abstract class MechanismWithStatus<S extends Record & Status> extends Mechanism
        implements StatusSource<S> {
    private S status = null;

    public final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    @Override
    /* package */ final void publishStatus() {
        status = reportStatus();
        StatusBus.getInstance().publishStatus(status);
    }

    protected abstract S reportStatus();
}

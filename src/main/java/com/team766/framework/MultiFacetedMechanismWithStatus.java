package com.team766.framework;

import java.util.NoSuchElementException;

public abstract class MultiFacetedMechanismWithStatus<S extends Record & Status>
        extends MultiFacetedMechanism implements StatusSource<S> {
    private S status = null;

    public final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    @Override
    /* package */ final void publishStatus() {
        status = updateStatus();
        StatusBus.getInstance().publishStatus(status);
    }

    protected abstract S updateStatus();
}

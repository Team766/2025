package com.team766.framework;

import java.util.NoSuchElementException;

public abstract class MultiFacetedMechanismWithStatus<S extends Record & Status>
        extends MultiFacetedMechanism implements StatusSource {
    private S status = null;

    public final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    @Override
    public boolean isStatusActive() {
        return true;
    }

    @Override
    /* package */ final void publishStatus() {
        status = updateStatus();
        StatusBus.getInstance().publishStatus(status, this);
    }

    protected abstract S updateStatus();
}

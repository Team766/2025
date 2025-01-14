package com.team766.framework3;

public abstract class MechanismWithStatus<S extends Record & Status> extends Mechanism
        implements StatusSource<S> {
    private S status = null;

    @Override
    /* package */ final void publishStatus() {
        status = reportStatus();
        StatusBus.getInstance().publishStatus(status);
    }

    protected abstract S reportStatus();
}

package com.team766.framework;

import java.util.Objects;

public abstract class MechanismWithRequests<R extends Request<S>, S extends Record & Status>
        extends MechanismWithStatus<S> {
    private R request = null;
    private boolean isRequestNew = false;

    @Override
    protected final void onMechanismIdle() {
        final var r = getIdleRequest();
        if (r != null) {
            setRequest(r);
        }
    }

    public final void setRequest(R request) {
        Objects.requireNonNull(request);
        checkContextReservation();
        this.request = request;
        isRequestNew = true;
        log(this.getClass().getName() + " processing request: " + request);
    }

    /**
     * The request returned by this method will be set as the request for this Mechanism when the
     * Mechanism is first created.
     *
     * If this Mechanism defines getIdleRequest(), then this Initial request will only be passed to
     * the first call to run() (after that, the Idle request may take over). Otherwise, this Initial
     * request will be passed to run() until something calls setRequest() on this Mechanism.
     *
     * This method will only be called once, immediately before the first call to run(). Because it
     * is called before run(), it cannot call getMechanismStatus() or otherwise depend on the Status
     * published by this Mechanism.
     */
    protected abstract R getInitialRequest();

    /**
     * The request returned by this method will be set as the request for this Mechanism when no
     * Procedures are reserving this Mechanism. This happens when a Procedure which reserved this
     * Mechanism completes. It can also happen when a Procedure that reserves this Mechanism is
     * preempted by another Procedure, but the new Procedure does not reserve this Mechanism.
     * getIdleRequest is especially in the latter case, because it can help to "clean up" after the
     * cancelled Procedure, returning this Mechanism back to some safe state.
     */
    protected R getIdleRequest() {
        return null;
    }

    @Override
    protected final void run() {
        if (request == null) {
            setRequest(getInitialRequest());
        }
        boolean wasRequestNew = isRequestNew;
        isRequestNew = false;
        run(request, wasRequestNew);
    }

    protected abstract void run(R request, boolean isRequestNew);
}

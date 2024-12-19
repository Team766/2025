package com.team766.framework3;

public final class RequestForSubmechanism<M extends Superstructure<M, ?>, E extends Mechanism<E, ?>>
        implements Request<M> {
    private final E mechanism;
    private final Request<E> request;
    private boolean firstTime = true;

    public RequestForSubmechanism(E mechanism, Request<E> request) {
        this.mechanism = mechanism;
        this.request = request;
    }

    @Override
    public boolean isDone() {
        return !firstTime && request.isDone();
    }

    @Override
    public void execute() {
        if (firstTime) {
            mechanism.setRequest(request);
            firstTime = false;
        }
    }

    @Override
    public void reset() {
        firstTime = true;
        request.reset();
    }
}

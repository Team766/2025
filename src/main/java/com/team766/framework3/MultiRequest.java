package com.team766.framework3;

public final class MultiRequest<M extends Mechanism<M, ?>> implements Request<M> {
    private final Request<M>[] requests;

    @SafeVarargs
    public MultiRequest(Request<M>... requests) {
        this.requests = requests;
    }

    @Override
    public boolean isDone() {
        for (var r : requests) {
            if (!r.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void execute() {
        for (var r : requests) {
            r.execute();
        }
    }

    @Override
    public void reset() {
        for (var r : requests) {
            r.reset();
        }
    }
}

package com.team766.framework3;

public abstract class InstantRequest<M extends Mechanism<M, ?>> implements Request<M> {
    private boolean isDone = false;

    @Override
    public final boolean isDone() {
        return isDone;
    }

    @Override
    public final void execute() {
        if (!isDone) {
            runOnce();
            isDone = true;
        }
    }

    @Override
    public void reset() {
        isDone = false;
    }

    protected abstract void runOnce();

    // TODO(MF3): do we need any way of checking if the request has been bumped/canceled?
}

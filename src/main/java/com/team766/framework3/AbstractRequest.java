package com.team766.framework3;

public abstract class AbstractRequest<M extends Mechanism<M, ?>> implements Request<M> {
    private boolean isDone = false;

    @Override
    public final boolean isDone() {
        return isDone;
    }

    public final void execute() {
        isDone = run();
    }

    public void reset() {
        isDone = false;
    }

    protected abstract boolean run();
}

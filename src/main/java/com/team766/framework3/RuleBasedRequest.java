package com.team766.framework3;

public abstract class RuleBasedRequest extends RuleEngine implements Mechanism.Directive {
    @Override
    public final boolean update() {
        this.run();

        return this.isDone();
    }

    protected abstract boolean isDone();
}

package com.team766.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class YieldedValues<T> implements ContextWithValue<T> {
    public static <T> ContextWithValue<T> discard(Context context) {
        return new YieldedValues<>(context, (__) -> {});
    }

    public static <T> RunnableWithContext discard(RunnableWithContextWithValue<T> procedure) {
        return (context) -> procedure.run(discard(context));
    }

    public static <T> List<T> runAndCollect(
            Context context, RunnableWithContextWithValue<T> procedure) {
        var result = new ArrayList<T>();
        procedure.run(new YieldedValues<>(context, result::add));
        return result;
    }

    private final Context parentContext;
    private final Consumer<T> valueCallback;

    private YieldedValues(Context parentContext, Consumer<T> valueCallback) {
        this.parentContext = parentContext;
        this.valueCallback = valueCallback;
    }

    @Override
    public boolean waitForConditionOrTimeout(BooleanSupplier predicate, double timeoutSeconds) {
        return parentContext.waitForConditionOrTimeout(predicate, timeoutSeconds);
    }

    @Override
    public void waitFor(BooleanSupplier predicate) {
        parentContext.waitFor(predicate);
    }

    @Override
    public void waitFor(LaunchedContext otherContext) {
        parentContext.waitFor(otherContext);
    }

    @Override
    public void waitFor(LaunchedContext... otherContexts) {
        parentContext.waitFor(otherContexts);
    }

    @Override
    public void yield() {
        parentContext.yield();
    }

    @Override
    public void waitForSeconds(double seconds) {
        parentContext.waitForSeconds(seconds);
    }

    @Override
    public LaunchedContext startAsync(RunnableWithContext func) {
        return parentContext.startAsync(func);
    }

    @Override
    public <U> LaunchedContextWithValue<U> startAsync(RunnableWithContextWithValue<U> func) {
        return parentContext.startAsync(func);
    }

    @Override
    public LaunchedContext startAsync(Runnable func) {
        return parentContext.startAsync(func);
    }

    @Override
    public void runSync(final RunnableWithContext func) {
        parentContext.runSync(func);
    }

    @Override
    public void takeOwnership(Mechanism mechanism) {
        parentContext.takeOwnership(mechanism);
    }

    @Override
    public void releaseOwnership(Mechanism mechanism) {
        parentContext.releaseOwnership(mechanism);
    }

    @Override
    public void yield(T valueToYield) {
        valueCallback.accept(valueToYield);
        parentContext.yield();
    }
}

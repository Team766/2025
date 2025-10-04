package com.team766.framework;

import com.team766.config.ConfigFileReader;
import com.team766.library.ValueProvider;
import org.littletonrobotics.junction.Logger;

public class Profiling {
    private static double getTime() {
        return System.currentTimeMillis();
    }

    public static class Scope implements AutoCloseable {
        private double start;
        private int index;
        private String name;

        private void start(String name, int index) {
            this.name = name;
            this.index = index;
            this.start = getTime();
        }

        @Override
        public void close() {
            if (index != scopeIndex - 1) {
                throw new IllegalStateException(
                        "Ended scope "
                                + name
                                + " but expected end of scope "
                                + scopes[scopeIndex - 1]);
            }
            --scopeIndex;
            addSample(name, getTime() - start);
            name = null;
        }
    }

    private static final ValueProvider<Boolean> profilingEnabled =
            ConfigFileReader.instance.getBoolean("profiling.enabled");
    private static final Scope[] scopes = new Scope[20];
    private static int scopeIndex = 0;

    static {
        for (int i = 0; i < scopes.length; ++i) {
            scopes[i] = new Scope();
        }
    }

    public static Scope scope(String name) {
        if (!profilingEnabled.valueOr(false)) {
            return null;
        }
        if (scopeIndex >= scopes.length) {
            return null;
        }
        final var scope = scopes[scopeIndex];
        scope.start(name, scopeIndex);
        ++scopeIndex;
        return scope;
    }

    public static void addSample(String name, double duration) {
        if (!profilingEnabled.valueOr(false)) {
            return;
        }
        Logger.recordOutput("Profiling/" + name, duration);
    }

    private Profiling() {}
}

package com.team766.framework;

public interface InstantContext {
    /**
     * Run the given Procedure synchronously (the calling Procedure will not resume until this one
     * has finished).
     * The given procedure must only reserve Mechanisms that were reserved by the calling Procedure.
     */
    void runSync(InstantProcedure procedure);
}

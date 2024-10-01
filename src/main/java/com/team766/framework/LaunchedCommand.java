package com.team766.framework;

import edu.wpi.first.wpilibj2.command.Command;

/**
 * This interface can be used by the caller to manage Commands launched asynchronously.
 */
public final class LaunchedCommand {
    private final Command command;

    public LaunchedCommand(Command command) {
        this.command = command;
    }

    /**
     * Returns a string meant to uniquely identify this Context (e.g. for use in
     * logging).
     */
    public String getName() {
        return command.getName();
    }

    /**
     * Returns true if this Context has finished running, false otherwise.
     */
    public boolean isFinished() {
        if (command.isFinished()) {
            return true;
        }
        if (!command.isScheduled()) {
            throw new IllegalStateException(getName() + " was canceled; it will never finish");
        }
        return false;
    }

    /**
     * Returns true if this Context was stopped before it finished, false otherwise.
     */
    public boolean isCancelled() {
        return !command.isScheduled() && command.isFinished();
    }

    /**
     * Interrupt the running of this Context and force it to terminate.
     */
    public void cancel() {
        command.cancel();
    }
}

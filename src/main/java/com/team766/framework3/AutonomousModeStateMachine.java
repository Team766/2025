package com.team766.framework3;

import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.Severity;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.function.Supplier;

public class AutonomousModeStateMachine {
    private enum AutonomousState {
        /**
         * {@link #autonomous} has not been started yet.
         * It can be scheduled the next time autonomousInit is called.
         */
        NEW,
        /**
         * {@link #autonomous} is currently running.
         */
        SCHEDULED,
        /**
         * {@link #autonomous} has finished, has been canceled, or is null.
         * A new instance of the autonomous Command needs to be created before
         * autonomous mode can be enabled.
         */
        INVALID,
    }

    private final Supplier<AutonomousMode> selector;
    private AutonomousMode autonMode = null;
    private Command autonomous = null;
    private AutonomousState autonState = AutonomousState.INVALID;

    public AutonomousModeStateMachine(Supplier<AutonomousMode> selector) {
        this.selector = selector;
    }

    public void stopAutonomousMode(final String reason) {
        if (autonState == AutonomousState.SCHEDULED) {
            autonomous.cancel();
            autonState = AutonomousState.INVALID;
            Logger.get(Category.AUTONOMOUS)
                    .logRaw(Severity.INFO, "Resetting autonomus procedure from " + reason);
        }
    }

    private void refreshAutonomousMode() {
        final AutonomousMode autonomousMode = selector.get();
        if (autonMode != autonomousMode) {
            stopAutonomousMode("selection of new autonomous mode " + autonomousMode);
            autonState = AutonomousState.INVALID;
        }
        if (autonState == AutonomousState.INVALID && autonomousMode != null) {
            autonomous = autonomousMode.instantiate();
            autonMode = autonomousMode;
            autonState = AutonomousState.NEW;
            Logger.get(Category.AUTONOMOUS)
                    .logRaw(
                            Severity.INFO,
                            "Initialized new autonomus procedure " + autonomous.getName());
        }
    }

    public void startAutonomousMode() {
        refreshAutonomousMode();
        switch (autonState) {
            case INVALID -> {
                Logger.get(Category.AUTONOMOUS)
                        .logRaw(Severity.WARNING, "No autonomous mode selected");
            }
            case SCHEDULED -> {
                Logger.get(Category.AUTONOMOUS)
                        .logRaw(
                                Severity.INFO,
                                "Continuing previous autonomus procedure "
                                        + autonomous.getName());
            }
            case NEW -> {
                autonomous.schedule();
                autonState = AutonomousState.SCHEDULED;
                Logger.get(Category.AUTONOMOUS)
                        .logRaw(
                                Severity.INFO,
                                "Starting new autonomus procedure " + autonomous.getName());
            }
        }
    }

    public void reinitializeAutonomousMode(final String reason) {
        stopAutonomousMode(reason);
        refreshAutonomousMode();
    }
}

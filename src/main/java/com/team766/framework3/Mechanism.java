package com.team766.framework3;

import com.team766.framework.StackTraceUtils;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public abstract class Mechanism<S extends Record & Status>
        implements Reservable, StatusSource<S>, LoggingBase {
    @FunctionalInterface
    public interface Directive {
        /**
         * @return True if the request has been completed; false if the request is in progress.
         */
        boolean update();
    }

    protected static Directive requestAllOf(Directive... directives) {
        return () -> {
            boolean isDone = true;
            for (var directive : directives) {
                if (!directive.update()) {
                    isDone = false;
                }
            }
            return isDone;
        };
        // TODO: Handle provenance tracking
    }

    private static final class MechanismRequest<M extends Mechanism<?>> extends Request<M> {
        public final Directive directive;
        public boolean isDone = false;

        public MechanismRequest(Directive directive) {
            this.directive = directive;

            if (directive.getClass().isSynthetic()) {
                // Probably a Lambda
                // TODO: Should we attempt to log any lambda captures?
            } else if (directive.getClass().isAnonymousClass()) {
            } else {
                // Normal class
                addProvenance(directive.toString());
            }
        }

        public void runDirective() {
            isDone = directive.update();
        }

        @Override
        public boolean isDone() {
            return isDone;
        }
    }

    private final SubsystemBase subsystem =
            new SubsystemBase() {
                @Override
                public String getName() {
                    return Mechanism.this.getName();
                }

                @Override
                public final void periodic() {
                    super.periodic();

                    if (superstructure != null) {
                        // This Mechanism's periodic() will be run by its superstructure.
                        return;
                    }

                    periodicInternal();
                }
            };

    private boolean isRunningPeriodic = false;

    private Superstructure<?> superstructure = null;

    private MechanismRequest<?> request = null;
    private S status = null;

    /**
     * This Command runs when no other Command (i.e. Procedure) is reserving this Mechanism.
     * If this Mechanism defines applyIdleRequest, this Command serves to apply that request.
     */
    private final class IdleCommand extends Command {
        public IdleCommand() {
            addRequirements(subsystem);
        }

        @Override
        public void initialize() {
            try {
                ReservingCommand.enterCommand(this);
                try {
                    applyIdleRequest();
                } finally {
                    ReservingCommand.exitCommand(this);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                LoggerExceptionUtils.logException(ex);
            }
        }

        @Override
        public boolean isFinished() {
            return false;
        }
    }

    public Mechanism() {
        subsystem.setDefaultCommand(new IdleCommand());
    }

    @Override
    public Category getLoggerCategory() {
        return Category.MECHANISMS;
    }

    /**
     * Indicate that this Mechanism is part of a superstructure.
     *
     * A Mechanism in a superstructure cannot be reserved individually by Procedures (Procedures
     * should reserve the entire superstructure) and cannot have an Idle request. Only the
     * superstructure should set requests on this Mechanism in its {@link #run(R, boolean)} method.
     *
     * @param superstructure The superstructure this Mechanism is part of.
     */
    /* package */ void setSuperstructure(Superstructure<?> superstructure) {
        Objects.requireNonNull(superstructure);
        if (this.superstructure != null) {
            throw new IllegalStateException("Mechanism is already part of a superstructure");
        }
        if (this.applyIdleRequest() != null) {
            throw new UnsupportedOperationException(
                    "A Mechanism contained in a superstructure cannot define an idle request. "
                            + "Use the superstructure's idle request to control the idle behavior "
                            + "of the contained Mechanisms.");
        }
        this.superstructure = superstructure;
    }

    protected final <M extends Mechanism<S>> Request<M> setRequest(Directive directive) {
        Objects.requireNonNull(directive);
        checkContextReservation();
        var newRequest = new MechanismRequest<M>(directive);
        this.request = newRequest;
        log(this.getName() + " processing request: " + request);
        return newRequest;
    }

    /**
     * The request returned by this method will be set as the request for this Mechanism when no
     * Procedures are reserving this Mechanism. This happens when a Procedure which reserved this
     * Mechanism completes. It can also happen when a Procedure that reserves this Mechanism is
     * preempted by another Procedure, but the new Procedure does not reserve this Mechanism.
     * applyIdleRequest is especially useful in the latter case, because it can help to "clean up"
     * after the cancelled Procedure, returning this Mechanism back to some safe state.
     */
    protected Request<?> applyIdleRequest() {
        return null;
    }

    /* package */ boolean isRunningPeriodic() {
        return isRunningPeriodic;
    }

    @Override
    public void checkContextReservation() {
        if (isRunningPeriodic()) {
            return;
        }
        if (superstructure != null) {
            if (!superstructure.isRunningPeriodic()) {
                var exception =
                        new IllegalStateException(
                                this.getName()
                                        + " is part of a superstructure but was used by something outside the superstructure");
                Logger.get(Category.FRAMEWORK)
                        .logRaw(
                                Severity.ERROR,
                                exception.getMessage()
                                        + "\n"
                                        + StackTraceUtils.getStackTrace(exception.getStackTrace()));
                throw exception;
            }
            return;
        }
        ReservingCommand.checkCurrentCommandHasReservation(subsystem);
    }

    @Override
    public final Set<Subsystem> getReservableSubsystems() {
        return Set.of(subsystem);
    }

    @Override
    public final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    /* package */ void periodicInternal() {
        try {
            status = reportStatus();
            publishStatus(status);

            isRunningPeriodic = true;
            request.runDirective();
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
        } finally {
            isRunningPeriodic = false;
        }
    }

    protected abstract S reportStatus();
}

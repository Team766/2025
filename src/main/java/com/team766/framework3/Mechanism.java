package com.team766.framework3;

import com.google.errorprone.annotations.DoNotCall;
import com.team766.framework.StackTraceUtils;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

class requestAllOf_must_have_at_least_one_argument {}

public abstract class Mechanism implements Reservable, LoggingBase {
    @FunctionalInterface
    public interface Directive {
        /**
         * @return True if the request has been completed; false if the request is in progress.
         */
        boolean update();

        default String getProvenance() {
            if (getClass().isSynthetic() // Synthetic implies this is probably a Lambda
                    // TODO: Should we attempt to log any lambda captures?
                    || getClass().isAnonymousClass()) {
                // TODO: This could be redundant with AOP provenance tracking
                final var declarationSite = getClass().getEnclosingMethod();
                return declarationSite != null
                        ? declarationSite.getDeclaringClass().getTypeName()
                                + '.'
                                + declarationSite.getName()
                        : "";
            } else {
                // Normal class
                return toString();
            }
        }
    }

    /**
     * @deprecated requestAllOf must have at least one argument
     */
    @DoNotCall
    @Deprecated
    protected static requestAllOf_must_have_at_least_one_argument requestAllOf() {
        return null;
    }

    protected static Directive requestAllOf(Directive... directives) {
        var sj = new StringJoiner("; ", "{", "}");
        for (int i = 0; i < directives.length; ++i) {
            sj.add(directives[i].getProvenance());
        }
        final String provenance = sj.toString();
        return new Directive() {
            @Override
            public boolean update() {
                boolean isDone = true;
                for (var directive : directives) {
                    if (!directive.update()) {
                        isDone = false;
                    }
                }
                return isDone;
            }

            @Override
            public String getProvenance() {
                return provenance;
            }
        };
    }

    protected static Directive dontWaitFor(Directive directive) {
        return new Directive() {
            @Override
            public boolean update() {
                directive.update();
                return true;
            }

            @Override
            public String getProvenance() {
                return "dontWaitFor " + directive.getProvenance();
            }
        };
    }

    private final class MechanismRequest<M extends Mechanism> extends Request<M> {
        public final Directive directive;
        public boolean isDone = false;

        public MechanismRequest(Directive directive) {
            this.directive = directive;

            addProvenance(directive.getProvenance());

            // Run once on construction to initialize isDone
            runDirective();
        }

        public void runDirective() {
            isDone = directive.update();
        }

        @Override
        public boolean isDone() {
            if (!isActive()) {
                throw new IllegalStateException(
                        "This request is no longer being run by " + getMechanism());
            }
            return isDone;
        }

        @Override
        public boolean isActive() {
            return request == this;
        }

        @Override
        Reservable getMechanism() {
            return Mechanism.this;
        }
    }

    private class ProxySubsystem extends SubsystemBase implements MechanismSubsystem {
        @Override
        public Reservable getMechanism() {
            return Mechanism.this;
        }

        @Override
        public String getName() {
            return Mechanism.this.getName();
        }

        @Override
        public final void periodic() {
            super.periodic();

            if (superstructure != null) {
                // This Mechanism's periodic() will be run by its Superstructure.
                return;
            }
            if (container != null) {
                // This Mechanism's periodic() will be run by the enclosing MultiFacetedMechanism.
                return;
            }

            periodicInternal();
        }
    }

    private final MechanismSubsystem subsystem = new ProxySubsystem();

    private boolean isRunningPeriodic = false;

    private Superstructure<?> superstructure = null;
    private MultiFacetedMechanism<?> container = null;

    private MechanismRequest<?> request = null;

    /**
     * This Command runs when no other Command (i.e. Procedure) is reserving this Mechanism.
     * If this Mechanism defines startIdleRequest, this Command serves to apply that request.
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
                    startIdleRequest();
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
        if (this.container != null) {
            throw new IllegalStateException("Mechanism is already part of a MultiFacetedMechanism");
        }
        if (this.startIdleRequest() != null) {
            throw new UnsupportedOperationException(
                    "A Mechanism contained in a superstructure cannot define an idle request. "
                            + "Use the superstructure's idle request to control the idle behavior "
                            + "of the contained Mechanisms.");
        }
        this.superstructure = superstructure;
    }

    /**
     * Indicate that this Mechanism is part of a MultiFacetedMechanism.
     */
    /* package */ void setContainer(MultiFacetedMechanism<?> container) {
        Objects.requireNonNull(container);
        if (this.superstructure != null) {
            throw new IllegalStateException("Mechanism is already part of a Superstructure");
        }
        if (this.container != null) {
            throw new IllegalStateException("Mechanism is already part of a MultiFacetedMechanism");
        }
        this.container = container;
    }

    protected final <M extends Mechanism> Request<M> startRequest(Directive directive) {
        if (isRunningPeriodic()) {
            throw new IllegalStateException("Cannot start a request while running another request");
        }
        checkContextReservation();
        Objects.requireNonNull(directive);
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
     * startIdleRequest is especially useful in the latter case, because it can help to "clean up"
     * after the cancelled Procedure, returning this Mechanism back to some safe state.
     */
    protected Request<?> startIdleRequest() {
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
    public final Set<? extends MechanismSubsystem> getReservableSubsystems() {
        return Set.of(subsystem);
    }

    /* package */ void periodicInternal() {
        try {
            isRunningPeriodic = true;
            request.runDirective();
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
        } finally {
            isRunningPeriodic = false;
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}

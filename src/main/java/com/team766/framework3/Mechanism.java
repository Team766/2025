package com.team766.framework3;

import com.team766.framework.StackTraceUtils;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Mechanism<S extends Record & Status>
        implements Reservable, StatusSource<S>, LoggingBase {
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

    private HashMap<Class<?>, Consumer<Object>> runRequestOverloads = new HashMap<>();

    private Superstructure<?> superstructure = null;

    private Request<? super S> request = null;
    private S status = null;

    /**
     * This Command runs when no other Command (i.e. Procedure) is reserving this Mechanism.
     * If this Mechanism defines getIdleRequest, this Command serves to apply that request.
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
                    final var r = getIdleRequest();
                    if (r != null) {
                        setRequest(r);
                    }
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
        populateSetRequestOverloads();

        subsystem.setDefaultCommand(new IdleCommand());
    }

    private void populateSetRequestOverloads() {
        var lookup = MethodHandles.lookup();

        for (final var method : getClass().getMethods()) {
            final var params = method.getParameterTypes();
            if (method.getName() != "runRequest" || params.length != 1) {
                continue;
            }
            @SuppressWarnings("rawtypes")
            final Class<? extends Request> requestParam;
            try {
                requestParam = params[0].asSubclass(Request.class);
            } catch (ClassCastException ex) {
                continue;
            }

            try {
                final var methodHandle = lookup.unreflect(method);
                final var site =
                        LambdaMetafactory.metafactory(
                                lookup,
                                // Name of the method in the functional interface (Consumer)
                                "accept",
                                // MethodType of the functional interface
                                MethodType.methodType(Consumer.class),
                                // Signature of the functional interface method (Consumer.accept)
                                // after
                                // type erasure
                                MethodType.methodType(void.class, Object.class),
                                // Handle to the method that's being wrapped
                                methodHandle,
                                // Signature of the method that's being wrapped
                                methodHandle.type());
                runRequestOverloads.put(
                        requestParam, (Consumer<Object>) site.getTarget().invokeExact());
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
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
        if (this.getIdleRequest() != null) {
            throw new UnsupportedOperationException(
                    "A Mechanism contained in a superstructure cannot define an idle request. "
                            + "Use the superstructure's idle request to control the idle behavior "
                            + "of the contained Mechanisms.");
        }
        this.superstructure = superstructure;
    }

    public final void setRequest(Request<? super S> request) {
        Objects.requireNonNull(request);
        if (!runRequestOverloads.containsKey(request.getClass())) {
            throw new IllegalArgumentException(
                    this.getName()
                            + " doesn't support requests of type "
                            + request.getClass().getName()
                            + " . Supported request types are "
                            + runRequestOverloads.keySet().stream()
                                    .map(Class::getName)
                                    .collect(Collectors.joining(", ")));
        }
        checkContextReservation();
        this.request = request;
        log(this.getName() + " processing request: " + request);
    }

    /**
     * The request returned by this method will be set as the request for this Mechanism when no
     * Procedures are reserving this Mechanism. This happens when a Procedure which reserved this
     * Mechanism completes. It can also happen when a Procedure that reserves this Mechanism is
     * preempted by another Procedure, but the new Procedure does not reserve this Mechanism.
     * getIdleRequest is especially in the latter case, because it can help to "clean up" after the
     * cancelled Procedure, returning this Mechanism back to some safe state.
     */
    protected Request<? super S> getIdleRequest() {
        return null;
    }

    /* package */ boolean isRunningPeriodic() {
        return isRunningPeriodic;
    }

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
    public final Subsystem getSubsystem() {
        return subsystem;
    }

    @Override
    public final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    public final Request<? super S> getRequest() {
        return request;
    }

    /* package */ void periodicInternal() {
        try {
            status = reportStatus();
            StatusBus.getInstance().publishStatus(status);

            isRunningPeriodic = true;
            runRequest();
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
        } finally {
            isRunningPeriodic = false;
        }
    }

    private void runRequest() {
        if (request == null) {
            return;
        }
        runRequestOverloads.get(request.getClass()).accept(request);
    }

    protected abstract S reportStatus();
}

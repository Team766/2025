package com.team766.framework;

import com.team766.logging.Category;
import com.team766.logging.LoggerExceptionUtils;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Objects;
import java.util.Set;
import org.littletonrobotics.junction.Logger;

public abstract class Mechanism implements Reservable, LoggingBase {
    private final class ProxySubsystem extends SubsystemBase implements MechanismSubsystem {
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
            if (container != null) {
                // This Mechanism's periodic() will be run by the enclosing MultiFacetedMechanism.
                return;
            }

            periodicInternal();
        }
    }

    private final MechanismSubsystem subsystem = new ProxySubsystem();

    private MultiFacetedMechanism container = null;

    private boolean isRunningPeriodic = false;

    /**
     * This Command runs when no other Command (i.e. Procedure) is reserving this Mechanism.
     * If this Mechanism defines onMechanismIdle, this Command serves to run that method.
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
                    onMechanismIdle();
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

    // This explicit override is needed because Reservable and LoggingBase both have a method called
    // getName().
    @Override
    public String getName() {
        return LoggingBase.super.getName();
    }

    @Override
    public Category getLoggerCategory() {
        return Category.MECHANISMS;
    }

    /**
     * Indicate that this Mechanism is part of a MultiFacetedMechanism.
     */
    /* package */ final void setContainer(MultiFacetedMechanism container) {
        Objects.requireNonNull(container);
        if (this.container != null) {
            throw new IllegalStateException("Mechanism is already part of a MultiFacetedMechanism");
        }
        this.container = container;
    }

    /**
     * This method is run once when no Procedures are reserving this Mechanism. This happens when a
     * Procedure which reserved this Mechanism completes. It can also happen when a Procedure that
     * reserves this Mechanism is preempted by another Procedure, but the new Procedure does not
     * reserve this Mechanism. onMechnaismIdle is especially useful in the latter case, because it
     * can help to "clean up" after the cancelled Procedure, returning this Mechanism back to some
     * safe state. Most Mechanisms should implement this in a way to causes the mechanism to stop
     * moving when it is not being used.
     */
    protected void onMechanismIdle() {}

    @Override
    public final void checkContextReservation() {
        if (isRunningPeriodic) {
            return;
        }
        ReservingCommand.checkCurrentCommandHasReservation(subsystem);
    }

    @Override
    public final Set<? extends MechanismSubsystem> getReservableSubsystems() {
        return Set.of(subsystem);
    }

    /* package */ final void periodicInternal() {
        try (var profileScope = Profiling.scope("Mechanisms/" + getName())) {
            publishStatus();

            isRunningPeriodic = true;

            Command command = CommandScheduler.getInstance().requiring(subsystem);
            if (command != null) {
                Logger.recordOutput("Mechanisms/" + getName(), command.toString());
            } else {
                Logger.recordOutput("Mechanisms/" + getName(), "<IDLE>");
            }

            run();
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
        } finally {
            isRunningPeriodic = false;
        }
    }

    protected void run() {}

    // Overridden in MechanismWithStatus
    /* package */ void publishStatus() {}

    @Override
    public final String toString() {
        return getName();
    }
}

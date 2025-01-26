package com.team766.framework3;

import com.team766.logging.Category;
import com.team766.logging.LoggerExceptionUtils;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class Mechanism extends SubsystemBase implements LoggingBase {
    private boolean isRunningPeriodic = false;

    /**
     * This Command runs when no other Command (i.e. Procedure) is reserving this Mechanism.
     * If this Mechanism defines onMechanismIdle, this Command serves to run that method.
     */
    private final class IdleCommand extends Command {
        public IdleCommand() {
            addRequirements(Mechanism.this);
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
        setDefaultCommand(new IdleCommand());
    }

    @Override
    public Category getLoggerCategory() {
        return Category.MECHANISMS;
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

    protected void checkContextReservation() {
        if (isRunningPeriodic) {
            return;
        }
        ReservingCommand.checkCurrentCommandHasReservation(this);
    }

    @Override
    public final void periodic() {
        super.periodic();

        try {
            publishStatus();

            isRunningPeriodic = true;
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
}

package com.team766.framework3;

import com.team766.logging.Category;
import com.team766.logging.LoggerExceptionUtils;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class MultiFacetedMechanism<S extends Record & Status> implements LoggingBase {
    @SuppressWarnings("unused")
    private SubsystemBase outerSubsystem =
            new SubsystemBase() {
                @Override
                public String getName() {
                    return MultiFacetedMechanism.this.getName();
                }

                @Override
                public final void periodic() {
                    super.periodic();

                    periodicInternal();
                }
            };

    private S status = null;

    @Override
    public Category getLoggerCategory() {
        return Category.MECHANISMS;
    }

    /* package */ void periodicInternal() {
        try {
            status = reportStatus();
            StatusBus.getInstance().publishStatus(status);

            run();
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
        }
    }

    protected abstract S reportStatus();

    protected abstract void run();
}

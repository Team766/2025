package com.team766.framework3;

import com.team766.logging.LoggerExceptionUtils;
import java.util.NoSuchElementException;

public abstract class MechanismWithStatus<S extends Record & Status> extends Mechanism {
    private S status = null;

    public final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    @Override
    /* package */ void periodicInternal() {
        try {
            status = reportStatus();
            StatusBus.getInstance().publishStatus(status);
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
            return;
        }
        super.periodicInternal();
    }

    protected abstract S reportStatus();
}

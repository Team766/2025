package com.team766.framework3;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import com.team766.framework.StackTraceUtils;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class MultiFacetedMechanism<S extends Record & Status>
        implements Reservable, LoggingBase {
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

    private HashSet<Subsystem> facetSubsystems = new HashSet<>();

    protected <M extends Mechanism<?>> M addFacet(M facet) {
        Objects.requireNonNull(facet);
        facet.setSuperstructure(this);
        facetSubsystems.addAll(facet.getReservableSubsystems());
        return facet;
    }

    protected <M extends MultiFacetedMechanism<S>> Request<M> requestOfFacets(
            Request<?>... facetRequests) {
        return new Request<M>() {
            @Override
            public boolean isDone() {
                for (var request : facetRequests) {
                    if (!request.isDone()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public void checkContextReservation() {
        for (var subsystem : facetSubsystems) {
            ReservingCommand.checkCurrentCommandHasReservation(subsystem);
        }
    }

    public final S getStatus() {
        if (status == null) {
            throw new NoSuchElementException(getName() + " has not published a status yet");
        }
        return status;
    }

    @Override
    public Set<Subsystem> getReservableSubsystems() {
        return facetSubsystems;
    }

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

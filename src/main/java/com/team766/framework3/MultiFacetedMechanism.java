package com.team766.framework3;

import com.team766.logging.Category;
import com.team766.logging.LoggerExceptionUtils;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public abstract class MultiFacetedMechanism<S extends Record & Status>
        implements Reservable, LoggingBase {
    private class ProxySubsystem extends SubsystemBase implements MechanismSubsystem {
        @Override
        public Reservable getMechanism() {
            return MultiFacetedMechanism.this;
        }

        @Override
        public String getName() {
            return MultiFacetedMechanism.this.getName();
        }

        @Override
        public final void periodic() {
            super.periodic();

            periodicInternal();
        }
    }

    @SuppressWarnings("unused")
    private final MechanismSubsystem outerSubsystem = new ProxySubsystem();

    private S status = null;

    private ArrayList<Mechanism> facets = new ArrayList<>();
    private HashSet<MechanismSubsystem> facetSubsystems = new HashSet<>();

    protected <M extends Mechanism> M addFacet(M facet) {
        Objects.requireNonNull(facet);
        facet.setContainer(this);
        addFacet(facet);
        facetSubsystems.addAll(facet.getReservableSubsystems());
        return facet;
    }

    protected <M extends MultiFacetedMechanism<S>> Request<M> requestOfFacets(
            Request<?>... facetRequests) {
        for (var facetRequest : facetRequests) {
            if (!facets.contains(facetRequest.getMechanism())) {
                throw new IllegalArgumentException(
                        "Request is for "
                                + facetRequest.getMechanism()
                                + " which is not a facet of "
                                + getName());
            }
        }
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

            @Override
            public boolean isActive() {
                for (var request : facetRequests) {
                    if (!request.isActive()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            Reservable getMechanism() {
                return MultiFacetedMechanism.this;
            }
        };
    }

    public void checkContextReservation() {
        if (facetSubsystems.isEmpty()) {
            throw new IllegalStateException("MultiFacetedMechanism does not have any Facets");
        }
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
    public Set<? extends MechanismSubsystem> getReservableSubsystems() {
        if (facetSubsystems.isEmpty()) {
            throw new IllegalStateException("MultiFacetedMechanism does not have any Facets");
        }
        return facetSubsystems;
    }

    @Override
    public Category getLoggerCategory() {
        return Category.MECHANISMS;
    }

    /* package */ void periodicInternal() {
        for (var m : facets) {
            m.periodicInternal();
        }

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

    @Override
    public String toString() {
        return getName();
    }
}

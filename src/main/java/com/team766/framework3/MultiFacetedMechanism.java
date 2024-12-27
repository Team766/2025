package com.team766.framework3;

import com.team766.logging.Category;
import com.team766.logging.LoggerExceptionUtils;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class MultiFacetedMechanism implements Reservable, LoggingBase {
    private final class ProxySubsystem extends SubsystemBase implements MechanismSubsystem {
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

    private final ArrayList<Mechanism> facets = new ArrayList<>();
    private final HashSet<MechanismSubsystem> facetSubsystems = new HashSet<>();

    protected final <M extends Mechanism> M addFacet(M facet) {
        Objects.requireNonNull(facet);
        facet.setContainer(this);
        addFacet(facet);
        facetSubsystems.addAll(facet.getReservableSubsystems());
        return facet;
    }

    protected final <M extends MultiFacetedMechanism> Request<M> requestOfFacets(
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

    public final void checkContextReservation() {
        if (facetSubsystems.isEmpty()) {
            throw new IllegalStateException("MultiFacetedMechanism does not have any Facets");
        }
        for (var subsystem : facetSubsystems) {
            ReservingCommand.checkCurrentCommandHasReservation(subsystem);
        }
    }

    @Override
    public final Set<? extends MechanismSubsystem> getReservableSubsystems() {
        if (facetSubsystems.isEmpty()) {
            throw new IllegalStateException("MultiFacetedMechanism does not have any Facets");
        }
        return facetSubsystems;
    }

    @Override
    public Category getLoggerCategory() {
        return Category.MECHANISMS;
    }

    /* package */ final void periodicInternal() {
        for (var m : facets) {
            m.periodicInternal();
        }

        try {
            publishStatus();

            run();
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggerExceptionUtils.logException(ex);
        }
    }

    // Overridden in MechanismWithStatus
    /* package */ void publishStatus() {}

    protected abstract void run();

    @Override
    public final String toString() {
        return getName();
    }
}

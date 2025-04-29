package com.team766.framework;

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

    protected class MechanismFacet extends Mechanism {}

    @SuppressWarnings("unused")
    private final MechanismSubsystem outerSubsystem = new ProxySubsystem();

    private final ArrayList<MechanismFacet> facets = new ArrayList<>();
    private final HashSet<MechanismSubsystem> facetSubsystems = new HashSet<>();

    private boolean isRunningPeriodic = false;

    protected final <M extends MechanismFacet> M addFacet(M facet) {
        Objects.requireNonNull(facet);
        facet.setContainer(this);
        facets.add(facet);
        facetSubsystems.addAll(facet.getReservableSubsystems());
        return facet;
    }

    @Override
    public final void checkContextReservation() {
        if (isRunningPeriodic) {
            return;
        }
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

    /* package */ final void periodicInternal() {
        for (var m : facets) {
            m.periodicInternal();
        }

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

    // Overridden in MultiFacetedMechanismWithStatus
    /* package */ void publishStatus() {}

    protected abstract void run();

    @Override
    public final String toString() {
        return getName();
    }
}

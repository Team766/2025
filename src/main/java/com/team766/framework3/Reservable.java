package com.team766.framework3;

import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.Set;

public interface Reservable {
    Set<? extends MechanismSubsystem> getReservableSubsystems();

    void checkContextReservation();

    String getName();
}

/* package */ interface MechanismSubsystem extends Subsystem {
    Reservable getMechanism();
}

package com.team766.framework3;

import java.util.Set;
import edu.wpi.first.wpilibj2.command.Subsystem;

public interface Reservable {
    Set<Subsystem> getReservableSubsystems();

    void checkContextReservation();
}

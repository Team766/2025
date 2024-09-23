package com.team766.framework;

import edu.wpi.first.wpilibj2.command.Command;
import java.util.Set;

public abstract class InstantProcedure extends Procedure {
    protected InstantProcedure() {
        super();
    }

    protected InstantProcedure(String name, Set<Reservable> reservations) {
        super(name, reservations);
    }

    protected abstract void run();

    @Override
    public final void run(Context context) {
        run();
    }

    @Override
    public Command createCommandToRunProcedure() {
        return new InstantCommand(this);
    }
}

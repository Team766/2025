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

    public abstract void run(InstantContext context);

    @SuppressWarnings("DontInvokeProcedureRunDirectly")
    @Override
    public final void run(Context context) {
        run(
                new InstantContext() {
                    @Override
                    public void runSync(InstantProcedure procedure) {
                        context.runSync(procedure);
                    }
                });
    }

    @Override
    public Command createCommandToRunProcedure() {
        return new InstantCommand(this);
    }
}

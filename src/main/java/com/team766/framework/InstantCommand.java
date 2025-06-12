package com.team766.framework;

import edu.wpi.first.wpilibj2.command.Command;

/* package */ final class InstantCommand extends Command {
    private final InstantProcedure procedure;

    public InstantCommand(InstantProcedure procedure) {
        this.procedure = procedure;
        for (var res : procedure.reservations()) {
            getRequirements().addAll(res.getReservableSubsystems());
        }
        setName(procedure.getName());
    }

    @SuppressWarnings("DontInvokeProcedureRunDirectly")
    @Override
    public void execute() {
        ReservingCommand.enterCommand(this);
        try {
            procedure.run(
                    new InstantContext() {
                        @Override
                        public void runSync(InstantProcedure p) {
                            p.checkReservations(InstantCommand.this);
                            p.run(this);
                        }
                    });
        } finally {
            ReservingCommand.exitCommand(this);
        }
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}

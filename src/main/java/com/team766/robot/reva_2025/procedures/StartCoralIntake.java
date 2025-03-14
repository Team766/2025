package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class StartCoralIntake extends Procedure {
    private Elevator elevator;
    private Wrist wrist;
    private CoralIntake coralIntake;

    public StartCoralIntake(Elevator elevator, Wrist wrist, CoralIntake coralIntake) {
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.coralIntake = reserve(coralIntake);
    }

    public void run(Context context) {
        context.runParallel(new CoralStationPositionAndIntake(elevator, wrist, coralIntake));
    }
}

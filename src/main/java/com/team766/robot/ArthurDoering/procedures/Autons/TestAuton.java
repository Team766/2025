package com.team766.robot.ArthurDoering.procedures.autons;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Intake;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;
import com.team766.robot.ArthurDoering.procedures.DriveProcedure;
import com.team766.robot.ArthurDoering.procedures.IntakeProcedure;
import com.team766.robot.ArthurDoering.procedures.ShootProcedure;

public class TestAuton extends Procedure {
    private Drive drive;
    private Shooter shoot;
    private Intake intake;

    public TestAuton(Drive drive, Shooter shoot, Intake intake) {
        this.drive = reserve(drive);
        this.shoot = reserve(shoot);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        context.runSync(new DriveProcedure(drive, 1));
        context.runSync(new IntakeProcedure(intake));
        //context.runSync(new ShootProcedure(shoot, 0.5));
    }
}

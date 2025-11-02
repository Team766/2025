package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.Kevan.mechanisms.Shooter;
import com.team766.robot.Kevan.mechanisms.Intake;
import com.team766.robot.Kevan.procedures.DriveProcedure;
import com.team766.robot.Kevan.procedures.ShootProcedure;
import com.team766.robot.Kevan.procedures.IntakeProcedure;

public class Autonomous extends Procedure{
    private Drive drive;
    private Shooter shooter;
    private Intake intake;

    public Autonomous(Drive myDrive, Shooter myShooter, Intake myIntake) {
        this.drive = reserve(myDrive);
        this.shooter = reserve(myShooter);
        this.intake = reserve(myIntake);
    }

    public void run(Context context) {
    }
}

package com.team766.robot.Filip.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Filip.mechanisms.Drive;
import com.team766.robot.Filip.mechanisms.Intake;
import com.team766.robot.Filip.mechanisms.Shooter;

public class Autonomous extends Procedure {
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

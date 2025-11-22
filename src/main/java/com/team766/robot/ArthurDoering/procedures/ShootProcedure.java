package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Intake;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;

public class ShootProcedure extends Procedure {

    private Shooter shooter;
    private Intake intake;
    private double motorPower;

    public ShootProcedure(Shooter myShooter, Intake myIntake, double motorPower) {
        shooter = reserve(myShooter);
        intake = reserve(myIntake);
        this.motorPower = motorPower;
    }

    public void run(Context context) {
        shooter.SetShooterSpeed(motorPower);
        context.waitForSeconds(0.5);
        shooter.SetTransferSpeed(1);
        context.waitForSeconds(1);
        intake.setIntake(1);
        shooter.SetTransferSpeed(1);
        context.waitForSeconds(1);
        intake.setIntake(0);
        shooter.SetTransferSpeed(0);
        shooter.SetShooterSpeed(0);
    }
}

package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;

public class ShootProcedure extends Procedure {

    private Shooter shooter;
    private double motorPower;

    public ShootProcedure(Shooter shooter, double motorPower) {
        this.shooter = reserve(shooter);
        this.motorPower = motorPower;
    }

    public void run(Context context) {
        shooter.SetShooterSpeed(motorPower);
        context.waitForSeconds(0.25);
        shooter.SetTransferSpeed(1);
        context.waitForSeconds(0.5);
        shooter.SetTransferSpeed(0);
        shooter.SetShooterSpeed(0);
    }
}

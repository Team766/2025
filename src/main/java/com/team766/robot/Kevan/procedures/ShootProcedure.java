package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Shooter;

public class ShootProcedure extends Procedure {

    private Shooter shooter;
    private double power;

    public ShootProcedure(Shooter myShooter, double power) {
        shooter = reserve(myShooter);
        this.power = power;
    }

    public void run(Context context) {
        shooter.SetShooterSpeed(power);
        context.waitForSeconds(1);
        shooter.SetTransferSpeed(1);
        context.waitForSeconds(0.5);
        shooter.SetTransferSpeed(0);
        shooter.SetShooterSpeed(0);
    }
}

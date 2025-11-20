package com.team766.robot.StephenCheung.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.StephenCheung.mechanisms.Shooter;

public class ShooterProcedure extends Procedure {

    private Shooter shooter;
    private double power;

    public ShooterProcedure(Shooter shooter, double power) {
        this.shooter = reserve(shooter);
        this.power = power;
    }

    public void run(Context context) {
        shooter.SetShooterSpeed(power);
        context.waitForSeconds(0.25);
        shooter.SetTransferSpeed(1);
        context.waitForSeconds(0.5);
        shooter.SetTransferSpeed(0);
        shooter.SetShooterSpeed(0);
    }
}

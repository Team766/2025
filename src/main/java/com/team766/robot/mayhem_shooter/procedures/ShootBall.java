package com.team766.robot.mayhem_shooter.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.mayhem_shooter.Lights;
import com.team766.robot.mayhem_shooter.mechanisms.Shooter;
import com.team766.robot.mayhem_shooter.mechanisms.Vision;

public class ShootBall extends Procedure {

    private Vision vision;
    private Shooter shooter;
    private Lights lights;

    public ShootBall(Vision vision, Shooter shooter, Lights lights) {
        this.vision = reserve(vision);
        this.shooter = reserve(shooter);
        this.lights = reserve(lights);
    }

    @Override
    public void run(Context context) {
        double speed = vision.getShooterSpeedFromDistance();
        if (speed == 0) {
            lights.setSolidColor(255, 0, 0); // Red for error
            return;
        }
        shooter.setShooterPower(speed);
        while (Math.abs(shooter.getShooterPower() - speed) > 5) {
            lights.setSolidColor(0, 255, 0); // Green while spinning up
            context.waitForSeconds(0.1);
            lights.setSolidColor(0, 0, 0); // Off for blink effect
            context.yield();
            context.waitForSeconds(0.1);
        }
        lights.setSolidColor(0, 255, 0); // Solid green when at speed
        shooter.enableFeeder();
        context.waitForSeconds(1);
        shooter.setFeederPower(0);
        shooter.stopShooterMotor();
    }
}

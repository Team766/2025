package com.team766.robot.mayhem_shooter.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.mayhem_shooter.mechanisms.Drive;
import com.team766.robot.mayhem_shooter.mechanisms.Shooter;
import com.team766.robot.mayhem_shooter.mechanisms.Vision;

public class Autonomous extends Procedure {

    private Vision vision;
    private Shooter shooter;
    private Drive drive;

    public Autonomous(Vision vision, Shooter shooter, Drive drive) {
        this.vision = reserve(vision);
        this.shooter = reserve(shooter);
        this.drive = reserve(drive);
    }

    @Override
    public void run(Context context) {
        drive.arcadeDrive(0, -0.25);
        context.waitForSeconds(3);
        drive.arcadeDrive(0, 0);
        shooter.setIntakeMotor(1);
        shooter.setShooterPower(0.45);
        drive.arcadeDrive(0.3, 0.25);
        context.waitForSeconds(1.2);
        drive.arcadeDrive(-0.375, -0.275);
        context.waitForSeconds(0.325);
        drive.arcadeDrive(0, 0);
        context.waitForSeconds(1);
        shooter.enableFeeder();
        context.waitForSeconds(1);
        shooter.setIntakeMotor(0);
        shooter.setFeederPower(0);
        shooter.stopShooterMotor();
        // Begin autonomus phase
        // Step 1: Drive forward and back into the ball
        //Step 2: Activate intake to collect ball
        // Step 3: Shoot at target using vision
    }
}

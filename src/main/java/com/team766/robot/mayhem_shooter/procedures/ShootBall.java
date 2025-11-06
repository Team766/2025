package com.team766.robot.mayhem_shooter.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.mayhem_shooter.mechanisms.Vision;
import com.team766.robot.mayhem_shooter.mechanisms.Shooter;

public class ShootBall extends Procedure {

    private Vision vision;
    private Shooter shooter;
    
    public ShootBall(Vision vision, Shooter shooter) {
        this.vision = reserve(vision);
        this.shooter = reserve(shooter);
    }
    @Override
    public void run(Context context) {
        double speed = vision.getShooterSpeedFromDistance();
        if (speed == 0){
            return;
        }
        shooter.setShooterPower(speed);
        context.waitForSeconds(3); // Need to adjust, possibly code check for speed
        shooter.enableFeeder();
        context.waitForSeconds(1);
        shooter.setFeederPower(0);
        shooter.stopShooterMotor();
    }
    
}

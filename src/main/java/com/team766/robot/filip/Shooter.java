package com.team766.robot.filip;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.RobotProvider;
import com.team766.hal.MotorController;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus>{
    MotorController shooter = RobotProvider.instance.getMotor("shooter");
    public record ShooterStatus(double pos_shooter, double motorSpeed) implements Status {}

    public Shooter(){

    }

    public void shootSpeed (double speed) {
            shooter.set(speed);
    }

    protected ShooterStatus updateStatus(){
        return new ShooterStatus(shooter.getSensorPosition(), shooter.getSensorVelocity());
    }
}

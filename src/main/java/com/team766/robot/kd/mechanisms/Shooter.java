package com.team766.robot.kd.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {
    MotorController shooterMotor = RobotProvider.instance.getMotor("shooter");

    public record ShooterStatus(double pos) implements Status {}

    public Shooter() {}

    public void run(double power) {
        shooterMotor.set(power);
    }

    public void stop() {
        shooterMotor.set(0);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(shooterMotor.getSensorPosition());
    }
}

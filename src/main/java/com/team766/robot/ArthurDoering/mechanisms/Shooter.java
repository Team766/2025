package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    public Shooter() {}

    MotorController ShooterMotor = (MotorController) RobotProvider.instance.getMotor("Motor");

    public record ShooterStatus(double currentPosition) implements Status {}

    public void setShooterSpeed(double power) {
        ShooterMotor.set(power);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(ShooterMotor.getSensorPosition());
    }
}

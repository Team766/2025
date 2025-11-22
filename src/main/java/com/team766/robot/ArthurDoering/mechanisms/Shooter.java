package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    public Shooter() {}

    MotorController shooterMotor =
            (MotorController) RobotProvider.instance.getMotor("shooter.shooterMotor");
    MotorController transferMotor =
            (MotorController) RobotProvider.instance.getMotor("shooter.transferMotor");

    public record ShooterStatus(double currentPosition) implements Status {}

    public void SetShooterSpeed(double motorPower) {
        shooterMotor.set(motorPower);
    }

    public void SetTransferSpeed(double motorPower) {
        transferMotor.set(motorPower);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(0);
    }
}

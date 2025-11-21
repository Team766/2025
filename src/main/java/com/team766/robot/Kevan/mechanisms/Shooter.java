package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    MotorController shooterMotor = RobotProvider.instance.getMotor("shooter.shooterMotor");
    MotorController transferMotor = RobotProvider.instance.getMotor("shooter.transferMotor");

    public record ShooterStatus(double pos_shooter, double pos_transfer) implements Status {}

    public Shooter() {}

    public void SetShooterSpeed(double motorPower) {
        shooterMotor.set(motorPower);
    }

    public void SetTransferSpeed(double motorPower) {
        transferMotor.set(motorPower);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(
                shooterMotor.getSensorPosition(), transferMotor.getSensorPosition());
    }
}

package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    MotorController shooter_motor = RobotProvider.instance.getMotor("shooter.shooterMotor");
    MotorController transfer_motor = RobotProvider.instance.getMotor("shooter.transferMotor");

    public record ShooterStatus(double pos_shooter, double pos_transfer) implements Status {}

    public Shooter() {}

    public void SetShooterSpeed(double motorPower) {
        shooter_motor.set(motorPower);
    }

    public void SetTransferSpeed(double motorPower) {
        transfer_motor.set(motorPower);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(
                shooter_motor.getSensorPosition(), transfer_motor.getSensorPosition());
    }
}

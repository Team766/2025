package com.team766.robot.StephenCheung.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    MotorController shooterMotor = RobotProvider.instance.getMotor("shooter_motor");
    MotorController pusherMotor = RobotProvider.instance.getMotor("transfer_motor");

    public record ShooterStatus(double pos_shooter, double pos_transfer) implements Status {}

    public Shooter() {}

    public void SetShooterSpeed(double motorPower) {
        shooterMotor.set(motorPower);
    }

    public void SetTransferSpeed(double motorPower) {
        pusherMotor.set(motorPower);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(shooterMotor.getSensorPosition(), pusherMotor.getSensorPosition());
    }
}

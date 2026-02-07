package com.team766.robot.filip.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    MotorController shooter_motor = RobotProvider.instance.getMotor("shooter_motor");
    MotorController tranfer_motor = RobotProvider.instance.getMotor("transfer_motor");

    public record ShooterStatus(double currentPosition) implements Status {}

    public Shooter() {}

    public void SetShooterSpeed(double motorPower) {
        shooter_motor.set(motorPower);
    }

    public void SetTransferSpeed(double motorPower) {
        tranfer_motor.set(motorPower);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(0);
    }
}

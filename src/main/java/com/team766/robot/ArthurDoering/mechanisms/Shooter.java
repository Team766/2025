package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    public Shooter() {}

    MotorController shooter_motor =
            (MotorController) RobotProvider.instance.getMotor("shooter_motor");
    MotorController transfer_motor =
            (MotorController) RobotProvider.instance.getMotor("transfer_motor");

    public record ShooterStatus(double currentPosition) implements Status {}

    public void SetShooterSpeed(double motorPower) {
        shooter_motor.set(motorPower);
    }

    public void SetTransferSpeed(double motorPower) {
        transfer_motor.set(motorPower);
    }

    protected ShooterStatus updateStatus() {
        return new ShooterStatus(0);
    }
}

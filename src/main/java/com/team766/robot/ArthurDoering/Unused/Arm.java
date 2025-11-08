package com.team766.robot.ArthurDoering.unused;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Arm extends MechanismWithStatus<Arm.ArmStatus> {

    public Arm() {}

    MotorController armMotor = (MotorController) RobotProvider.instance.getMotor("Motor");

    public record ArmStatus(double currentPosition) implements Status {}

    public void setMotorPower(double motorPower) {
        armMotor.set(motorPower);
    }

    protected ArmStatus updateStatus() {
        return new ArmStatus(armMotor.getSensorPosition());
    }
}

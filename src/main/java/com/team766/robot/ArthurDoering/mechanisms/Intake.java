package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {

    public Intake() {}

    MotorController IntakeMotor = (MotorController) RobotProvider.instance.getMotor("Motor");

    public record IntakeStatus(double currentPosition) implements Status {}

    public void setIntake(double motorPower) {
        IntakeMotor.set(motorPower);
    }

    protected IntakeStatus updateStatus() {
        return new IntakeStatus(IntakeMotor.getSensorPosition());
    }
}

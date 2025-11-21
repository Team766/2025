package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {

    MotorController intakeMotor = RobotProvider.instance.getMotor("intake.intakeMotor");

    public record IntakeStatus(double currentPosition) implements Status {}

    public Intake() {}

    public void SetIntake(double motorPower) {
        intakeMotor.set(motorPower);
    }

    protected IntakeStatus updateStatus() {
        return new IntakeStatus(intakeMotor.getSensorPosition());
    }
}

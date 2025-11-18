package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {

    public Intake() {}

    MotorController IntakeMotor1 = (MotorController) RobotProvider.instance.getMotor("intake.intakeMotor");
    MotorController IntakeMotor2 = (MotorController) RobotProvider.instance.getMotor("intake.intakeMotor");

    public record IntakeStatus(double currentPosition1, double currentPosition2)
            implements Status {}

    public void setIntake(double motorPower) {
        IntakeMotor1.set(motorPower);
        IntakeMotor2.set(motorPower);
    }

    protected IntakeStatus updateStatus() {
        return new IntakeStatus(IntakeMotor1.getSensorPosition(), IntakeMotor2.getSensorPosition());
    }
}

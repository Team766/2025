package com.team766.robot.kd.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {
    MotorController IntakeMotor = RobotProvider.instance.getMotor("intake");
    double power;

    public record IntakeStatus(double pos) implements Status {}

    public Intake(double intake_power) {
        power = intake_power;
    }

    public void stop() {
        IntakeMotor.set(0);
    }

    public void run() {
        IntakeMotor.set(power);
    }

    protected IntakeStatus updateStatus() {
        return new IntakeStatus(IntakeMotor.getSensorPosition());
    }
}

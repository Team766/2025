package com.team766.robot.copy_2910.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {

    private MotorController climberMotor;

    public static record ClimberStatus() implements Status {}

    public Climber() {
        climberMotor = (MotorController) RobotProvider.instance.getMotor("ClimberMotor");
    }

    public void setClimberSpeed(double speed) {
        climberMotor.set(speed);
    }

    public void stop() {
        climberMotor.set(0.0);
    }

    protected ClimberStatus updateStatus() {
        return new ClimberStatus();
    }
}

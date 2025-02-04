package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private static final double POWER = 1.0;
    private MotorController leftClimberMotor;
    private MotorController rightClimberMotor;

    public record ClimberStatus(double angle) implements Status {}

    public Climber() {
        leftClimberMotor = RobotProvider.instance.getMotor("climber.leftMotor");
        rightClimberMotor = RobotProvider.instance.getMotor("climber.rightMotor");
        rightClimberMotor.follow(leftClimberMotor);
        leftClimberMotor.setSensorPosition(0);
    }

    public void climberDown() {
        leftClimberMotor.set(POWER);
    }

    public void climberUp() {
        leftClimberMotor.set(-POWER);
    }

    @Override
    protected void onMechanismIdle() {
        leftClimberMotor.set(0.0);
    }

    @Override
    protected ClimberStatus updateStatus() {
        return new ClimberStatus(leftClimberMotor.getSensorPosition());
    }
}

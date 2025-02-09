package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private MotorController leftClimberMotor;
    private MotorController rightClimberMotor;
    private double HIGH_LIMIT = 90;

    public static record ClimberStatus(double currentPower) implements Status {}

    public Climber() {
        leftClimberMotor = RobotProvider.instance.getMotor(CLIMBER_LEFT_MOTOR);
        rightClimberMotor = RobotProvider.instance.getMotor(CLIMBER_RIGHT_MOTOR);
        rightClimberMotor.follow(leftClimberMotor);
        leftClimberMotor.setNeutralMode(NeutralMode.Brake);
        rightClimberMotor.setNeutralMode(NeutralMode.Brake);
        leftClimberMotor.setSoftLimits(HIGH_LIMIT, 0);
        rightClimberMotor.setSoftLimits(HIGH_LIMIT, 0);
        leftClimberMotor.enableSoftLimits(true);
        rightClimberMotor.enableSoftLimits(true);
    }

    public void climbUp() {
        leftClimberMotor.set(1);
    }

    public void climbDown() {
        leftClimberMotor.set(-1);
    }

    public void climbOff() {
        leftClimberMotor.set(0);
    }

    @Override
    protected void onMechanismIdle() {
        climbOff();
    }

    @Override
    protected ClimberStatus updateStatus() {
        return new ClimberStatus(leftClimberMotor.getSensorPosition());
    }
}

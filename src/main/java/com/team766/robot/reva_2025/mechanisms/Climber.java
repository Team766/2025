package com.team766.robot.reva_2025.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva.mechanisms.MotorUtil;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private MotorController leftClimberMotor;
    private MotorController rightClimberMotor;
    private double HIGH_LIMIT = 90;
    private double CLIMBER_POWER = 0.5;

    public static record ClimberStatus(double currentPower) implements Status {}

    public Climber() {
        leftClimberMotor = RobotProvider.instance.getMotor(ConfigConstants.CLIMBER_LEFT_MOTOR);
        rightClimberMotor = RobotProvider.instance.getMotor(ConfigConstants.CLIMBER_RIGHT_MOTOR);
        leftClimberMotor.setNeutralMode(NeutralMode.Brake);
        rightClimberMotor.setNeutralMode(NeutralMode.Brake);
        MotorUtil.setSoftLimits(leftClimberMotor, HIGH_LIMIT, 0);
        MotorUtil.setSoftLimits(rightClimberMotor, HIGH_LIMIT, 0);
        MotorUtil.enableSoftLimits(leftClimberMotor, true);
        MotorUtil.enableSoftLimits(rightClimberMotor, true);
        rightClimberMotor.follow(leftClimberMotor);
        leftClimberMotor.setSensorPosition(0);
    }

    public void climb(double multiplier) {
        leftClimberMotor.set(CLIMBER_POWER * multiplier);
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

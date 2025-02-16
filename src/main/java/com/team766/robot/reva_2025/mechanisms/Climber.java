package com.team766.robot.reva_2025.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.CLIMBER_LEFT_MOTOR;
import static com.team766.robot.reva.constants.ConfigConstants.CLIMBER_RIGHT_MOTOR;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import static com.team766.robot.reva.constants.ConfigConstants.CLIMBER_LEFT_MOTOR;
import static com.team766.robot.reva.constants.ConfigConstants.CLIMBER_RIGHT_MOTOR;
import com.team766.robot.reva.mechanisms.MotorUtil;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private MotorController leftClimberMotor;
    private MotorController rightClimberMotor;
    private double HIGH_LIMIT = 90;
    private double UP_POWER = 0.5;
    private double DOWN_POWER = -0.5;

    public static record ClimberStatus(double currentPower) implements Status {}

    public Climber() {
        leftClimberMotor = RobotProvider.instance.getMotor(CLIMBER_LEFT_MOTOR);
        rightClimberMotor = RobotProvider.instance.getMotor(CLIMBER_RIGHT_MOTOR);
        leftClimberMotor.setNeutralMode(NeutralMode.Brake);
        rightClimberMotor.setNeutralMode(NeutralMode.Brake);
        MotorUtil.setSoftLimits(leftClimberMotor, HIGH_LIMIT, 0);
        MotorUtil.setSoftLimits(rightClimberMotor, HIGH_LIMIT, 0);
        MotorUtil.enableSoftLimits(leftClimberMotor, true);
        MotorUtil.enableSoftLimits(rightClimberMotor, true);
        rightClimberMotor.follow(leftClimberMotor);
        leftClimberMotor.setSensorPosition(0);
    }

    public void climbUp() {
        leftClimberMotor.set(UP_POWER);
    }

    public void climbDown() {
        leftClimberMotor.set(DOWN_POWER);
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

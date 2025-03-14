package com.team766.robot.reva_2025.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private MotorController leftClimberMotor;
    private double HIGH_LIMIT = 90; // TODO: use absolute encoder soft limit
    private double CLIMBER_POWER = 1.0;

    public static record ClimberStatus(double currentPower) implements Status {}

    public Climber() {
        leftClimberMotor = RobotProvider.instance.getMotor(ConfigConstants.CLIMBER_LEFT_MOTOR);
        leftClimberMotor.setNeutralMode(NeutralMode.Brake);
        // MotorUtil.setSoftLimits(leftClimberMotor, HIGH_LIMIT, 0);
        // MotorUtil.enableSoftLimits(leftClimberMotor, true);
        leftClimberMotor.setSensorPosition(0);
    }

    public void climb(double sign) {
        leftClimberMotor.set(CLIMBER_POWER * Math.signum(sign));
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

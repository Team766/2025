package com.team766.robot.reva_2025.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.robot.reva.mechanisms.MotorUtil;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private MotorController leftClimberMotor;
    private double HIGH_LIMIT = 90; // TODO: use absolute encoder soft limit
    private double CLIMBER_POWER = 1.0;
    private State state;
    private boolean movingToStartPosition = false;
    private ValueProvider<Double> ffGain;

    public static record ClimberStatus(double currentPower, State state) implements Status {}

    public enum State {
        // velocity is in revolutions per minute
        On,
        Off,
        Done
    }

    public Climber() {
        leftClimberMotor = RobotProvider.instance.getMotor(ConfigConstants.CLIMBER_LEFT_MOTOR);
        leftClimberMotor.setNeutralMode(NeutralMode.Brake);
        MotorUtil.setSoftLimits(leftClimberMotor, -122, -400);
        leftClimberMotor.setSensorPosition(0);
        ffGain = ConfigFileReader.instance.getDouble(ConfigConstants.CLIMBER_FFGAIN);
        state = State.Off;
    }

    public void climb(double sign) {
        movingToStartPosition = false;
        if (sign < 0) { // up
            MotorUtil.enableSoftLimits(leftClimberMotor, false);
        } else {
            MotorUtil.enableSoftLimits(leftClimberMotor, true);
        }
        leftClimberMotor.set(CLIMBER_POWER * Math.signum(sign));
        state = State.On;
    }

    public void moveToStartPosition() {
        movingToStartPosition = true;
    }

    public void climbOff() {
        leftClimberMotor.set(0);
        state = State.Off;
    }

    @Override
    protected void run() {
        if (movingToStartPosition) {
            leftClimberMotor.set(MotorController.ControlMode.Position, 0.0, ffGain.valueOr(0.0));
        }
    }

    @Override
    protected void onMechanismIdle() {
        climbOff();
        state = State.Done;
    }

    @Override
    protected ClimberStatus updateStatus() {
        return new ClimberStatus(leftClimberMotor.get(), state);
    }
}

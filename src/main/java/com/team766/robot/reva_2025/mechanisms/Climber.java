package com.team766.robot.reva_2025.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.MotorController.ControlMode;
import com.team766.robot.reva.mechanisms.MotorUtil;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private MotorController climberMotor;
    private double CLIMBER_POWER = 1.0;

    public static record ClimberStatus(double currentPower, double currentPosition) implements Status {}

    public enum ClimbPosition {
        TOP(-400),
        CLIMB(-122),
        BOTTOM(0);

        private double motorRotations;

        private ClimbPosition(double motorRotations) {
            this.motorRotations = motorRotations;
        }

        public double getPosition() {
            return motorRotations;
        }
    }

    public Climber() {
        climberMotor = RobotProvider.instance.getMotor(ConfigConstants.CLIMBER_LEFT_MOTOR);
        climberMotor.setNeutralMode(NeutralMode.Brake);
        MotorUtil.setSoftLimits(climberMotor, -122, -400);
        climberMotor.setSensorPosition(0);
    }

    public void runClimb(double sign) {
        if (sign <= 0) { //up
            MotorUtil.enableSoftLimits(climberMotor, false);
        } else {
            MotorUtil.enableSoftLimits(climberMotor, true);
        }
        climberMotor.set(CLIMBER_POWER * Math.signum(sign));
    }

    public void moveClimber(ClimbPosition position) {
        MotorUtil.enableSoftLimits(climberMotor, false);
        climberMotor.set(ControlMode.Position, position.getPosition());
    }

    @Override
    protected void onMechanismIdle() {
        runClimb(0);
    }

    @Override
    protected ClimberStatus updateStatus() {
        return new ClimberStatus(climberMotor.get(), climberMotor.getSensorPosition());
    }
}

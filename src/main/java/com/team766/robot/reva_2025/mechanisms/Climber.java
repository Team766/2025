package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {
    private MotorController climberMotor;
    private Position position;

    public static record ClimberStatus(double currentPower) implements Status {}

    public enum Position {
        CLIMBER_TOP(1),
        CLIMBER_BOTTOM(-1),
        CLIMBER_IDLE(0);
        private double power;

        Position(double power) {
            this.power = power;
        }

        public double getPower() {
            return power;
        }
    }

    public Climber() {
        climberMotor = RobotProvider.instance.getMotor("climber.Motor");
        position = Position.CLIMBER_IDLE;
    }

    public void climbUp() {
        position = Position.CLIMBER_TOP;
        climberMotor.set(position.getPower());
    }

    public void climbDown() {
        position = Position.CLIMBER_BOTTOM;
        climberMotor.set(position.getPower());
    }

    public void climbOff() {
        position = Position.CLIMBER_IDLE;
        climberMotor.set(position.getPower());
    }

    @Override
    protected ClimberStatus updateStatus() {
        return new ClimberStatus(climberMotor.getSensorPosition());
    }
}

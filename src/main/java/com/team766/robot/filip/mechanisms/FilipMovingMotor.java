package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {

    public MovingMotor() {}

    MotorController motor = RobotProvider.instance.getMotor("mainMotor");

    public void move(double motorPower) {
        motor.set(motorPower);
    }

    public record MovingMotorStatus(double currentPosition) implements Status {}

    public void stop() {
        motor.set(0);
    }

    protected MovingMotorStatus updateStatus() {
        return new MovingMotorStatus(motor.getSensorPosition());
    }
}

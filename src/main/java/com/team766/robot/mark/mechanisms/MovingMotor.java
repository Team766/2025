package com.team766.robot.mark.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
    MotorController motor = RobotProvider.instance.getMotor("leftMotor");
    public record MovingMotorStatus(double currentPosition) implements Status {
        
    }

    public MovingMotor() {}

    public void move(double speed) {
        motor.set(speed);
    }

    protected MovingMotorStatus updateStatus() {
        return new MovingMotorStatus(67);
    }
}
package com.team766.robot.Rookie_Training.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus <MovingMotor.MovingMotorStatus> {

    public MovingMotor() {

    }
    
    public record MovingMotorStatus(double currentPosition) implements Status {
    }

    public void setMotorPower(double power) {
        motor.set(power)
    }

    protected MovingMotorStatus updateStatus(){
        return new MovingMotorStatus(0)
    }
}

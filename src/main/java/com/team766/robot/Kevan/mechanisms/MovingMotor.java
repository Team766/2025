package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
    
    public MovingMotor() {
 
    }
    public record MovingMotorStatus(double currentPosition) implements Status {

    }
    public void MoveMotor(double motorPower) {
        motor.set(motorPower);
    }
    protected MovingMotorStatus updateStatus() {
        return new MovingMotorStatus(currentPosition:0);
        motor.getSensorPosition();
    }
 }
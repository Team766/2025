package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.hal.RobotProvider;
import com.team766.simulator.elements.MotorController;

public class Drive extends MechanismWithStatus<drive.DriveStatus{
    
    MotorController leftMotor = RobotProvider.instance.getMotor("LeftMotor");
    MotorController rightMotor = RobotProvider.instance.getMotor("RightMotor");
    public record DriveStatus(double pos_leftMotor, double pos_rightMotor) implements Status {}

    public Drive() {
        
    }
    public void move_left(double motorPower) {
        leftMotor.set(motorPower);
    }
    public void move_right(double motorPower) {
        rightMotor.set(motorPower);
    }
    protected DriveStatus updateStatus() {
        return new DriveStatus(leftMotor.getSensorPosition(), rightMotor.getSensorPosition());
    }
}

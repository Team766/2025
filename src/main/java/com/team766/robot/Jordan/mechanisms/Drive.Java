package com.team766.robot.Jordan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {

    public MotorController motor1;
    public MotorController motor2;

    public Drive () {
        motor1 = RobotProvider.instance.getMotor("motorLeft");
        motor2 = RobotProvider.instance.getMotor("motorRight");
    }
    
    public record DriveStatus(double currentPositionLeft, double currentPositionRight) implements Status {
    }

    protected DriveStatus updateStatus() {
        return new DriveStatus(motor1.getSensorPosition(), motor2.getSensorPosition());
    }

    public void setLeftMotorPower(double powerLeft) {
        motor1.set(powerLeft);
    }
    public void setRightMotorPower(double powerRight) {
        motor2.set(powerRight);
    }
    
    //MotorController.getSensorPosition();
    //MotorController.setPosition();
    //MotorController.setCurrentLimit();
    //MotorController.follow();
    //MotorController.setInverted();
    
}
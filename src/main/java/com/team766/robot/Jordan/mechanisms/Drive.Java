package com.team766.robot.Jordan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {

    public MotorController motor1;
    public MotorController motor2;
    public MotorController motor3;
    public MotorController motor4;

    public Drive () {
        motor1 = RobotProvider.instance.getMotor("motorLeft");
        motor2 = RobotProvider.instance.getMotor("motorRight");
        motor3 = RobotProvider.instance.getMotor("motorLoad");
        motor4 = RobotProvider.instance.getMotor("motorIntake");
    }
    
    public record DriveStatus(double currentPositionLeft, double currentPositionRight, double currentPositionLoad, double currentPositionIntake) implements Status {
    }

    protected DriveStatus updateStatus() {
        return new DriveStatus(motor1.getSensorPosition(), motor2.getSensorPosition(),motor3.getSensorPosition(),motor4.getSensorPosition());
    }

    public void setLeftMotorPower(double powerLeft) {
        motor1.set(powerLeft);
    }
    public void setRightMotorPower(double powerRight) {
        motor2.set(powerRight);
    }
    public void setLoadMotorPower(double powerLoad) {
        motor3.set(powerLoad);
    }
    public void setIntakeMotorPower(double powerIntake) {
        motor4.set(powerIntake);
    }


    //MotorController.getSensorPosition();
    //MotorController.setPosition();
    //MotorController.setCurrentLimit();
    //MotorController.follow();
    //MotorController.setInverted();

}
package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {

    MotorController leftMotor = RobotProvider.instance.getMotor("leftMotor");
    MotorController rightMotor = RobotProvider.instance.getMotor("rightMotor");

    public record DriveStatus(double pos_leftMotor, double pos_rightMotor) implements Status {}

    public Drive() {}

    public void turn_left(double motorPower) {
        rightMotor.set(motorPower);
        leftMotor.set(-motorPower);
    }

    public void turn_right(double motorPower) {
        leftMotor.set(motorPower);
        rightMotor.set(-motorPower);
    }

    public void move_right(double motorPower) {
        leftMotor.set(motorPower);
    }

    public void move_left(double motorPower) {
        rightMotor.set(motorPower);
    }

    public void move_straight(double motorPower) {
        leftMotor.set(motorPower);
        rightMotor.set(motorPower);
    }

    protected DriveStatus updateStatus() {
        return new DriveStatus(leftMotor.getSensorPosition(), rightMotor.getSensorPosition());
    }
}

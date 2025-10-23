package com.team766.robot.mark.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
    MotorController motor_left = RobotProvider.instance.getMotor("leftMotor");
    MotorController motor_right = RobotProvider.instance.getMotor("rightMotor");
    public record DriveStatus(double pos_motor_left, double pos_motor_right) implements Status {}

    public Drive() {}

    public void stop() {
        move_left(0);
        move_right(0);
    }

    public void move_forward(double power) {
        move_left(power);
        move_right(power);
    }

    public void move_back(double power) {
        move_left(-power);
        move_right(-power);
    }

    public void move_left(double power) {
        motor_left.set(power);
    }

    public void move_right(double power) {
        motor_right.set(power);
    }

    protected DriveStatus updateStatus() {
        return new DriveStatus(motor_left.getSensorPosition(), motor_right.getSensorPosition());
    }
}

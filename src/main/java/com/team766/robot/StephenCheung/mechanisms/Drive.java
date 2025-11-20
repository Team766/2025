package com.team766.robot.StephenCheung.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.mechanisms.SwerveDrive.DriveStatus;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {

    MotorController motorLeft = RobotProvider.instance.getMotor("leftMotor");
    MotorController motorRight = RobotProvider.instance.getMotor("rightMotor");

    public Drive() {}

    public record DriveStatus(double leftPosition, double rightPosition) implements Status {}

    public void turn_left(double motorPower) {
        motorRight.set(motorPower);
        motorLeft.set(-motorPower);
    }

    public void turn_right(double motorPower) {
        motorLeft.set(motorPower);
        motorRight.set(-motorPower);
    }

    public void move_straight(double motorPower) {
        motorLeft.set(motorPower);
        motorRight.set(motorPower);
    }

    public void move_right(double motorPower) {
        motorRight.set(motorPower);
    }

    public void move_left(double motorPower) {
        motorRight.set(motorPower);
    }

    protected DriveStatus updateStatus() {
        return new DriveStatus(motorLeft.getSensorPosition(), motorRight.getSensorPosition());
    }
}

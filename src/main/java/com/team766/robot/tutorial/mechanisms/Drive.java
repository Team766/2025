package com.team766.robot.tutorial.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
    public record DriveStatus(double distance, double leftDistance, double rightDistance)
            implements Status {}

    private MotorController leftMotor;
    private MotorController rightMotor;
    private EncoderReader leftEncoder;
    private EncoderReader rightEncoder;

    public Drive() {
        leftMotor = RobotProvider.instance.getMotor("drive.leftMotor");
        rightMotor = RobotProvider.instance.getMotor("drive.rightMotor");
        leftEncoder = RobotProvider.instance.getEncoder("drive.leftEncoder");
        rightEncoder = RobotProvider.instance.getEncoder("drive.rightEncoder");
    }

    @Override
    public Category getLoggerCategory() {
        return Category.DRIVE;
    }

    public void setDrivePower(double leftPower, double rightPower) {
        leftMotor.set(leftPower);
        rightMotor.set(rightPower);
    }

    public void setArcadeDrivePower(double forward, double turn) {
        double leftMotorPower = turn + forward;
        double rightMotorPower = -turn + forward;
        setDrivePower(leftMotorPower, rightMotorPower);
    }

    public void resetEncoders() {
        leftEncoder.reset();
        rightEncoder.reset();
    }

    protected DriveStatus updateStatus() {
        double leftValue = leftEncoder.getDistance();
        double rightValue = rightEncoder.getDistance();
        return new DriveStatus((leftValue + rightValue) / 2, leftValue, rightValue);
    }
}

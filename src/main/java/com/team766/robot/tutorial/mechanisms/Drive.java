package com.team766.robot.tutorial.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.GyroReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
    public record DriveStatus(double x, double y, double angle) implements Status {}

    private MotorController leftMotor;
    private MotorController rightMotor;
    private EncoderReader leftEncoder;
    private EncoderReader rightEncoder;
    private GyroReader gyro;

    // variables for calculating position using odometry
    private double x;
    private double y;
    private double previousLeft;
    private double previousRight;
    private double previousAngle;
    private double velocity;
    private double previousTime = RobotProvider.getTimeProvider().get();

    public Drive() {
        leftMotor = RobotProvider.instance.getMotor("drive.leftMotor");
        rightMotor = RobotProvider.instance.getMotor("drive.rightMotor");
        leftEncoder = RobotProvider.instance.getEncoder("drive.leftEncoder");
        rightEncoder = RobotProvider.instance.getEncoder("drive.rightEncoder");
        gyro = RobotProvider.instance.getGyro("drive.gyro");
    }

    @Override
    protected DriveStatus updateStatus() {
        double leftDistance = leftEncoder.getDistance();
        double rightDistance = rightEncoder.getDistance();
        double distance = ((leftDistance - previousLeft) + (rightDistance - previousRight)) / 2.0;

        // Use trapezoidal integration to get better position accuracy
        x += Math.cos(previousAngle) * distance / 2;
        y += Math.sin(previousAngle) * distance / 2;
        double angle = gyro.getAngle() % 360;
        x += Math.cos(angle) * distance / 2;
        y += Math.sin(angle) * distance / 2;

        previousLeft = leftDistance;
        previousRight = rightDistance;
        previousAngle = angle;

        // calculate velocity
        double currentTime = RobotProvider.getTimeProvider().get();
        velocity = distance / (currentTime - previousTime);
        previousTime = currentTime;

        return new DriveStatus(x, y, angle);
    }

    public void resetGyro() {
        gyro.reset();
    }

    public void setDrivePower(double leftPower, double rightPower) {
        leftMotor.set(leftPower);
        rightMotor.set(rightPower);
    }

    public void setArcadeDrive(double fwdPower, double turnPower) {
        double maximum = Math.max(Math.abs(fwdPower), Math.abs(turnPower));
        double total = fwdPower + turnPower;
        double difference = fwdPower - turnPower;

        if (fwdPower >= 0) {
            if (turnPower >= 0) {
                setDrivePower(maximum, difference);
            } else {
                setDrivePower(total, maximum);
            }
        } else {
            if (turnPower >= 0) {
                setDrivePower(total, -maximum);
            } else {
                setDrivePower(-maximum, difference);
            }
        }
    }
}

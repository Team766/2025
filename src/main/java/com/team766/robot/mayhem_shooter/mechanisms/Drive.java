package com.team766.robot.mayhem_shooter.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {

    private MotorController leftMotor = RobotProvider.instance.getMotor("drive.leftMotor");
    private MotorController rightMotor = RobotProvider.instance.getMotor("drive.rightMotor");

    public static record DriveStatus() implements Status {}

    // TODO: Check for inverted motors
    public void arcadeDrive(double x, double y) {
        leftMotor.set(y + x);
        rightMotor.set(y - x);
    }

    protected void onMechanismIdle() {
        // Stop mechanism when nothing is using it.
        leftMotor.set(0);
        rightMotor.set(0);
    }

    @Override
    protected DriveStatus updateStatus() {
        return new DriveStatus();
    }
}

package com.team766.robot.mark.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
    MotorController motor1 = RobotProvider.instance.getMotor("leftMotor");
    MotorController motor2 = RobotProvider.instance.getMotor("rightMotor");
    public record DriveStatus(double pos_motor1, double pos_motor2) implements Status {}

    public Drive() {}

    public void move_left(double speed) {
        motor1.set(speed);
    }

    public void move_right(double speed) {
        motor2.set(speed);
    }

    protected DriveStatus updateStatus() {
        return new DriveStatus(motor1.getSensorPosition(), motor2.getSensorPosition());
    }
}

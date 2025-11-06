
package com.team766.robot.filip.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
    MotorController motor_left = RobotProvider.instance.getMotor("leftMotor");
    MotorController motor_right = RobotProvider.instance.getMotor("rightMotor");
    public record DriveStatus(double pos_motor_left, double pos_motor_right) implements Status {}

    public Drive() {}

    public void move_left(double speed) {
        motor_left.set(speed);
    }

    public void move_right(double speed) {
        motor_right.set(speed);
    }

    protected DriveStatus updateStatus() {
        return new DriveStatus(motor_left.getSensorPosition(), motor_right.getSensorPosition());
    }
}

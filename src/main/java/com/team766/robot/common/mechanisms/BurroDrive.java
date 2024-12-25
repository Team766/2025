package com.team766.robot.common.mechanisms;

import static com.team766.robot.common.constants.ConfigConstants.*;

import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;

public class BurroDrive extends Mechanism<BurroDrive.DriveStatus> {
    public record DriveStatus() implements Status {}

    public Request<BurroDrive> requestPercentOutput(
            double leftTargetPercentOutput, double rightTargetPercentOutput) {
        return setRequest(
                requestAllOf(
                        leftMotor.requestPercentOutput(leftTargetPercentOutput),
                        rightMotor.requestPercentOutput(rightTargetPercentOutput)));
    }

    /**
     * @param forward how much power to apply to moving the robot (positive being forward)
     * @param turn how much power to apply to turning the robot (positive being CCW)
     */
    public Request<BurroDrive> requestArcadeDrive(double forward, double turn) {
        return requestPercentOutput(forward - turn, forward + turn);
    }

    private final MotorController leftMotor;
    private final MotorController rightMotor;

    public BurroDrive() {
        leftMotor = RobotProvider.instance.getMotor(DRIVE_LEFT);
        rightMotor = RobotProvider.instance.getMotor(DRIVE_RIGHT);
    }

    @Override
    public Category getLoggerCategory() {
        return Category.DRIVE;
    }

    @Override
    protected Request<BurroDrive> applyIdleRequest() {
        return requestPercentOutput(0, 0);
    }

    @Override
    protected DriveStatus reportStatus() {
        return new DriveStatus();
    }
}

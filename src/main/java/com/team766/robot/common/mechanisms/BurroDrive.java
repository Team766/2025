package com.team766.robot.common.mechanisms;

import static com.team766.robot.common.constants.ConfigConstants.*;

import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;

public class BurroDrive extends Mechanism<BurroDrive.DriveStatus> {
    public record DriveStatus(double leftPercentOutput, double rightPercentOutput)
            implements Status {}

    public record PercentOutputRequest(
            double leftTargetPercentOutput, double rightTargetPercentOutput)
            implements Request<DriveStatus> {
        @Override
        public boolean isDone(DriveStatus status) {
            return leftTargetPercentOutput == status.leftPercentOutput()
                    && rightTargetPercentOutput == status.rightPercentOutput();
        }
    }

    /**
     * @param forward how much power to apply to moving the robot (positive being forward)
     * @param turn how much power to apply to turning the robot (positive being CCW)
     */
    public record ArcadeDriveRequest(double forward, double turn) implements Request<DriveStatus> {
        PercentOutputRequest toPercentOutputRequest() {
            return new PercentOutputRequest(forward - turn, forward + turn);
        }

        @Override
        public boolean isDone(DriveStatus status) {
            return toPercentOutputRequest().isDone(status);
        }
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

    protected void runRequest(PercentOutputRequest request) {
        leftMotor.set(request.leftTargetPercentOutput());
        rightMotor.set(request.rightTargetPercentOutput());
    }

    @Override
    protected Request<DriveStatus> getIdleRequest() {
        return new PercentOutputRequest(0, 0);
    }

    @Override
    protected DriveStatus reportStatus() {
        return new DriveStatus(leftMotor.get(), rightMotor.get());
    }
}

package com.team766.robot.burro_arm.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.requests.PercentOutputRequest;
import com.team766.framework3.requests.PercentOutputStatus;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.math.Math;

public class Gripper extends Mechanism<Gripper.GripperStatus> {
    public record GripperStatus(double percentOutput) implements PercentOutputStatus {}

    public static Request<? super GripperStatus> requestForIdle() {
        final double idlePower =
                -ConfigFileReader.instance.getDouble("gripper.idlePower").valueOr(0.05);
        return new PercentOutputRequest(idlePower);
    }

    public static Request<? super GripperStatus> requestForIntake() {
        final double intakePower =
                -ConfigFileReader.instance.getDouble("gripper.intakePower").valueOr(0.3);
        return new PercentOutputRequest(intakePower);
    }

    public static Request<? super GripperStatus> requestForOuttake() {
        final double outtakePower =
                ConfigFileReader.instance.getDouble("gripper.outtakePower").valueOr(0.1);
        return new PercentOutputRequest(outtakePower);
    }

    private final MotorController leftMotor;
    private final MotorController rightMotor;

    public Gripper() {
        leftMotor = RobotProvider.instance.getMotor("gripper.leftMotor");
        rightMotor = RobotProvider.instance.getMotor("gripper.rightMotor");
    }

    protected void runRequest(PercentOutputRequest request) {
        leftMotor.setRequest(request);
        rightMotor.setRequest(request);
    }

    @Override
    protected Request<? super GripperStatus> getIdleRequest() {
        return requestForIdle();
    }

    @Override
    protected GripperStatus reportStatus() {
        return new GripperStatus(Math.absMax(leftMotor.get(), rightMotor.get()));
    }
}

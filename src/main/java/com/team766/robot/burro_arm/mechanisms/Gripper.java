package com.team766.robot.burro_arm.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Gripper extends Mechanism<Gripper.GripperStatus> {
    public record GripperStatus() implements Status {}

    private final MotorController leftMotor;
    private final MotorController rightMotor;

    private final double idlePower =
            -ConfigFileReader.instance.getDouble("gripper.idlePower").valueOr(0.05);
    private final double intakePower =
            -ConfigFileReader.instance.getDouble("gripper.intakePower").valueOr(0.3);
    private final double outtakePower =
            ConfigFileReader.instance.getDouble("gripper.outtakePower").valueOr(0.1);

    public Gripper() {
        leftMotor = RobotProvider.instance.getMotor("gripper.leftMotor");
        rightMotor = RobotProvider.instance.getMotor("gripper.rightMotor");
    }

    public Request<Gripper> requestIdle() {
        return requestPercentOutput(idlePower);
    }

    public Request<Gripper> requestIntake() {
        return requestPercentOutput(intakePower);
    }

    public Request<Gripper> requestOuttake() {
        return requestPercentOutput(outtakePower);
    }

    private Request<Gripper> requestPercentOutput(double percentOutput) {
        return setRequest(
                requestAllOf(
                        leftMotor.requestPercentOutput(percentOutput),
                        rightMotor.requestPercentOutput(percentOutput)));
    }

    @Override
    protected Request<Gripper> applyIdleRequest() {
        return requestIdle();
    }

    @Override
    protected GripperStatus reportStatus() {
        return new GripperStatus();
    }
}

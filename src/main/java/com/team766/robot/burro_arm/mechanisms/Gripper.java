package com.team766.robot.burro_arm.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.InstantRequest;
import com.team766.framework3.Mechanism;
import com.team766.framework3.MultiRequest;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.framework3.requests.RequestForPercentOutput;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Gripper extends Mechanism<Gripper, Gripper.GripperStatus> {
    public record GripperStatus() implements Status {}

    private class RequestForMotorPower extends InstantRequest<Gripper> {
        private double power;

        public RequestForMotorPower(double power) {
            this.power = power;
        }

        @Override
        protected void runOnce() {
            leftMotor.set(power);
            rightMotor.set(power);
        }
    }

    private Request<Gripper> requestForMotorPower(double power) {
        return new MultiRequest<Gripper>(
                new RequestForPercentOutput<>(leftMotor, power),
                new RequestForPercentOutput<>(rightMotor, power));
    }

    public Request<Gripper> requestForIdle() {
        return new RequestForMotorPower(idlePower);
    }

    public Request<Gripper> requestForIntake() {
        return new RequestForMotorPower(intakePower);
    }

    public Request<Gripper> requestForOuttake() {
        return new RequestForMotorPower(outtakePower);
    }

    private final MotorController leftMotor;
    private final MotorController rightMotor;
    private final double intakePower =
            -ConfigFileReader.instance.getDouble("gripper.intakePower").valueOr(0.3);
    private final double outtakePower =
            ConfigFileReader.instance.getDouble("gripper.outtakePower").valueOr(0.1);
    private final double idlePower =
            -ConfigFileReader.instance.getDouble("gripper.idlePower").valueOr(0.05);

    public Gripper() {
        leftMotor = RobotProvider.instance.getMotor("gripper.leftMotor");
        rightMotor = RobotProvider.instance.getMotor("gripper.rightMotor");
    }

    @Override
    protected Request<Gripper> getIdleRequest() {
        return requestForIdle();
    }

    @Override
    protected GripperStatus reportStatus() {
        return new GripperStatus();
    }
}

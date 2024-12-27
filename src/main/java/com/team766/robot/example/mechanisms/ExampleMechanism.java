package com.team766.robot.example.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class ExampleMechanism extends MechanismWithStatus<ExampleMechanism.ExampleMechanismStatus> {
    public record ExampleMechanismStatus() implements Status {}

    public Request<ExampleMechanism> requestMotorPower(double leftPower, double rightPower) {
        checkContextReservation();
        return startRequest(
                requestAllOf(
                        leftMotor.requestPercentOutput(leftPower),
                        rightMotor.requestPercentOutput(rightPower)));
    }

    private MotorController leftMotor;
    private MotorController rightMotor;

    public ExampleMechanism() {
        leftMotor = RobotProvider.instance.getMotor("exampleMechanism.leftMotor");
        rightMotor = RobotProvider.instance.getMotor("exampleMechanism.rightMotor");
    }

    @Override
    protected Request<ExampleMechanism> startIdleRequest() {
        return requestMotorPower(0, 0);
    }

    @Override
    protected ExampleMechanismStatus reportStatus() {
        return new ExampleMechanismStatus();
    }
}

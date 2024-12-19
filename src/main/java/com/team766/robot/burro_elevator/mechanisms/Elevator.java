package com.team766.robot.burro_elevator.mechanisms;

import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.framework3.requests.RequestForPercentOutput;
import com.team766.framework3.requests.RequestForPositionControl;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;

public class Elevator extends Mechanism<Elevator, Elevator.ElevatorStatus> {
    public static final double BOTTOM_POSITION = 100.0; // set this proper
    public static final double TOP_POSITION = 0.0; // set this proper

    public record ElevatorStatus(double position) implements Status {}

    public Request<Elevator> requestForPower(double power) {
        return new RequestForPercentOutput<Elevator>(motor, power);
    }

    public Request<Elevator> requestForPosition(double position) {
        return new RequestForPositionControl<Elevator>(
                motor,
                position / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION,
                POSITION_TOLERANCE / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION,
                STOPPED_SPEED_THRESHOLD / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION * 60 /* RPMs */,
                0.0);
    }

    public Request<Elevator> requestForHoldPosition() {
        final double currentPosition = getStatus().position();
        return requestForPosition(currentPosition);
    }

    public Request<Elevator> requestForNudgeUp() {
        final double currentPosition = getStatus().position();
        return requestForPosition(currentPosition + NUDGE_UP_INCREMENT);
    }

    public Request<Elevator> requestForNudgeDown() {
        final double currentPosition = getStatus().position();
        return requestForPosition(currentPosition - NUDGE_DOWN_INCREMENT);
    }

    private static final double NUDGE_UP_INCREMENT = 1.0; // inches
    private static final double NUDGE_DOWN_INCREMENT = 1.0; // inches

    private static final double POSITION_TOLERANCE = 0.5; // inches
    private static final double STOPPED_SPEED_THRESHOLD = 0.5; // inches/second

    private static final double MOTOR_ROTATIONS_TO_ELEVATOR_POSITION =
            (0.25 /*chain pitch = distance per tooth*/)
                    * (18. /*teeth per rotation of sprocket*/)
                    * (1. / (3. * 4. * 4.) /*planetary gearbox*/);

    private final CANSparkMaxMotorController motor;

    public Elevator() {
        motor = (CANSparkMaxMotorController) RobotProvider.instance.getMotor("elevator.Motor");
        motor.setSmartCurrentLimit(10, 80, 200);
    }

    @Override
    protected Request<Elevator> getIdleRequest() {
        return requestForHoldPosition();
    }

    @Override
    protected ElevatorStatus reportStatus() {
        return new ElevatorStatus(motor.getSensorPosition() * MOTOR_ROTATIONS_TO_ELEVATOR_POSITION);
    }
}

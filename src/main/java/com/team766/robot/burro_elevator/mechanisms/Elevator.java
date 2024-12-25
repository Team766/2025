package com.team766.robot.burro_elevator.mechanisms;

import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;

public class Elevator extends Mechanism<Elevator.ElevatorStatus> {
    public static final double BOTTOM_POSITION = 100.0; // set this proper
    public static final double TOP_POSITION = 0.0; // set this proper

    public record ElevatorStatus(double position, double velocity) implements Status {}

    public Request<Elevator> requestPosition(double targetPosition) {
        checkContextReservation();
        return setRequest(
                motor.requestPosition(
                        targetPosition / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION,
                        POSITION_TOLERANCE / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION,
                        STOPPED_SPEED_THRESHOLD
                                / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION
                                * 60.0 /* RPMs */));
    }

    public Request<Elevator> requestHoldPosition() {
        checkContextReservation();
        return requestPosition(getStatus().position());
    }

    public Request<Elevator> requestNudgeUp() {
        checkContextReservation();
        return requestPosition(getStatus().position() + NUDGE_UP_INCREMENT);
    }

    public Request<Elevator> requestNudgeDown() {
        checkContextReservation();
        return requestPosition(getStatus().position() - NUDGE_DOWN_INCREMENT);
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
    protected Request<Elevator> applyIdleRequest() {
        return requestHoldPosition();
    }

    @Override
    protected ElevatorStatus reportStatus() {
        return new ElevatorStatus(
                motor.getSensorPosition() * MOTOR_ROTATIONS_TO_ELEVATOR_POSITION,
                motor.getSensorVelocity() /* RPMs */ / 60.0 * MOTOR_ROTATIONS_TO_ELEVATOR_POSITION);
    }
}

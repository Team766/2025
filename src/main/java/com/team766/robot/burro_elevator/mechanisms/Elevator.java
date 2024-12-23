package com.team766.robot.burro_elevator.mechanisms;

import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.requests.PercentOutputRequest;
import com.team766.framework3.requests.PositionRequest;
import com.team766.framework3.requests.PositionStatus;
import com.team766.framework3.requests.VelocityStatus;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;

public class Elevator extends Mechanism<Elevator.ElevatorStatus> {
    public static final double BOTTOM_POSITION = 100.0; // set this proper
    public static final double TOP_POSITION = 0.0; // set this proper

    public record ElevatorStatus(
            double position, double positionTolerance, double velocity, double velocityTolerance)
            implements PositionStatus, VelocityStatus {}

    public void setRequestToHoldPosition() {
        checkContextReservation();
        setRequest(new PositionRequest(getStatus().position()));
    }

    public void setRequestToNudgeUp() {
        checkContextReservation();
        setRequest(new PositionRequest(getStatus().position() + NUDGE_UP_INCREMENT));
    }

    public void setRequestToNudgeDown() {
        checkContextReservation();
        setRequest(new PositionRequest(getStatus().position() - NUDGE_DOWN_INCREMENT));
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
    protected Request<? super ElevatorStatus> getIdleRequest() {
        return new PositionRequest(getStatus().position());
    }

    protected void runRequest(PercentOutputRequest request) {
        motor.setRequest(request);
    }

    protected void runRequest(PositionRequest request) {
        motor.setRequest(
                new PositionRequest(
                        request.targetPosition() / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION));
    }

    @Override
    protected ElevatorStatus reportStatus() {
        return new ElevatorStatus(
                motor.getSensorPosition() * MOTOR_ROTATIONS_TO_ELEVATOR_POSITION,
                POSITION_TOLERANCE,
                motor.getSensorVelocity() /* RPMs */ / 60.0 * MOTOR_ROTATIONS_TO_ELEVATOR_POSITION,
                STOPPED_SPEED_THRESHOLD);
    }
}

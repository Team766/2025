package com.team766.robot.gatorade.mechanisms;

import static com.team766.framework3.RulePersistence.ONCE;

import com.team766.framework3.MultiRequest;
import com.team766.framework3.Request;
import com.team766.framework3.RequestForSubmechanism;
import com.team766.framework3.Rule;
import com.team766.framework3.RuleEngine;
import com.team766.framework3.Status;
import com.team766.framework3.Superstructure;
import com.team766.robot.gatorade.PlacementPosition;
import com.team766.robot.gatorade.mechanisms.Intake.GamePieceType;
import java.util.Set;

public class Arm extends Superstructure<Arm, Arm.ArmStatus> {
    public record ArmStatus() implements Status {}

    private final Shoulder shoulder;
    private final Elevator elevator;
    private final Wrist wrist;

    public Arm() {
        this.shoulder = addMechanism(new Shoulder());
        this.elevator = addMechanism(new Elevator());
        this.wrist = addMechanism(new Wrist());
    }

    // NOTE: This is private because we don't want to expose the ability
    // to send arbitrary requests to the individual mechanisms.
    private Request<Arm> simpleRequest(
            Request<Shoulder> shoulderRequest,
            Request<Elevator> elevatorRequest,
            Request<Wrist> wristRequest) {
        return new MultiRequest<Arm>(
                new RequestForSubmechanism<>(shoulder, shoulderRequest),
                new RequestForSubmechanism<>(elevator, elevatorRequest),
                new RequestForSubmechanism<>(wrist, wristRequest));
    }

    public Request<Arm> requestForStop() {
        return simpleRequest(
                shoulder.requestForStop(), elevator.requestForStop(), wrist.requestForStop());
    }

    public Request<Arm> requestForHoldPosition() {
        return simpleRequest(
                shoulder.requestForHoldPosition(),
                elevator.requestForHoldPosition(),
                wrist.requestForHoldPosition());
    }

    public Request<Arm> requestForNudgeShoulderUp() {
        return simpleRequest(
                shoulder.requestForNudgeUp(),
                elevator.requestForHoldPosition(),
                wrist.requestForHoldPosition());
    }

    public Request<Arm> requestForNudgeShoulderDown() {
        return simpleRequest(
                shoulder.requestForNudgeDown(),
                elevator.requestForHoldPosition(),
                wrist.requestForHoldPosition());
    }

    public Request<Arm> requestForNudgeElevatorUp() {
        return simpleRequest(
                shoulder.requestForHoldPosition(),
                elevator.requestForNudgeUp(),
                wrist.requestForHoldPosition());
    }

    public Request<Arm> requestForNudgeElevatorDown() {
        return simpleRequest(
                shoulder.requestForHoldPosition(),
                elevator.requestForNudgeDown(),
                wrist.requestForHoldPosition());
    }

    public Request<Arm> requestForNudgeWristUp() {
        return simpleRequest(
                shoulder.requestForHoldPosition(),
                elevator.requestForHoldPosition(),
                wrist.requestForNudgeUp());
    }

    public Request<Arm> requestForNudgeWristDown() {
        return simpleRequest(
                shoulder.requestForHoldPosition(),
                elevator.requestForHoldPosition(),
                wrist.requestForNudgeDown());
    }

    public class RequestForPosition implements Request<Arm> {
        private final double shoulderSetpoint;
        private final Request<Shoulder> shoulderSetpointRequest;
        private final Request<Elevator> elevatorSetpointRequest;
        private final Request<Wrist> wristSetpointRequest;
        private final RuleEngine ruleEngine;

        public RequestForPosition(
                double shoulderSetpoint, double elevatorSetpoint, double wristSetpoint) {
            this.shoulderSetpoint = shoulderSetpoint;
            shoulderSetpointRequest = shoulder.requestForPosition(shoulderSetpoint);
            elevatorSetpointRequest = elevator.requestForPosition(elevatorSetpoint);
            wristSetpointRequest = wrist.requestForPosition(wristSetpoint);

            ruleEngine = new RuleEngine();
            ruleEngine.addRule(
                    Rule.create(
                                    "ELEVATOR_IN_POSITION",
                                    () -> elevator.getStatus().isNearTo(elevatorSetpoint))
                            .withOnTriggeringProcedure(
                                    ONCE,
                                    Set.of(shoulder, elevator, wrist),
                                    () -> {
                                        // If lowering the shoulder, do that after
                                        // the elevator.
                                        shoulder.setRequest(shoulderSetpointRequest);

                                        elevator.setRequest(elevatorSetpointRequest);

                                        // Lastly, move the wrist.
                                        wrist.setRequest(wristSetpointRequest);
                                    }));

            ruleEngine.addRule(
                    Rule.create(
                                    "PREPARE_TO_MOVE_ELEVATOR",
                                    () ->
                                            // Always retract the wrist before
                                            // moving the elevator.
                                            // It might already be retracted, so
                                            // it's possible that this step finishes
                                            // instantaneously.
                                            !wrist.getStatus().isNearTo(Wrist.Position.RETRACTED)
                                                    ||
                                                    // If raising the shoulder, do
                                                    // that before the elevator
                                                    // (else, lower it after the
                                                    // elevator).
                                                    (isRaisingShoulder()
                                                            && !shoulder.getStatus()
                                                                    .isNearTo(shoulderSetpoint)))
                            .withOnTriggeringProcedure(
                                    ONCE,
                                    Set.of(shoulder, elevator, wrist),
                                    () -> {
                                        shoulder.setRequest(
                                                isRaisingShoulder()
                                                        ? shoulder.requestForPosition(
                                                                shoulderSetpoint)
                                                        : shoulder.requestForHoldPosition());

                                        elevator.setRequest(elevator.requestForHoldPosition());

                                        wrist.setRequest(
                                                wrist.requestForPosition(Wrist.Position.RETRACTED));
                                    }));

            ruleEngine.addRule(
                    Rule.create("MOVING_ELEVATOR", () -> true)
                            .withOnTriggeringProcedure(
                                    ONCE,
                                    Set.of(shoulder, elevator, wrist),
                                    () -> {
                                        shoulder.setRequest(
                                                isRaisingShoulder()
                                                        ? shoulder.requestForPosition(
                                                                shoulderSetpoint)
                                                        : shoulder.requestForHoldPosition());

                                        // Move the elevator until it gets near the
                                        // target position.
                                        elevator.setRequest(
                                                elevator.requestForPosition(elevatorSetpoint));

                                        wrist.setRequest(
                                                wrist.requestForPosition(Wrist.Position.RETRACTED));
                                    }));
        }

        private boolean isRaisingShoulder() {
            return shoulderSetpoint > shoulder.getStatus().angle();
        }

        @Override
        public boolean isDone() {
            return shoulderSetpointRequest.isDone()
                    && elevatorSetpointRequest.isDone()
                    && wristSetpointRequest.isDone();
        }

        @Override
        public void execute() {
            ruleEngine.run();
        }

        @Override
        public void reset() {}
    }

    public Request<Arm> requestForRetraced() {
        return new RequestForPosition(
                Shoulder.Position.BOTTOM, Elevator.Position.RETRACTED, Wrist.Position.RETRACTED);
    }

    public Request<Arm> requestForExtendedToLow() {
        return new RequestForPosition(
                Shoulder.Position.FLOOR, Elevator.Position.LOW, Wrist.Position.LEVEL);
    }

    public Request<Arm> requestForExtendedToMid() {
        return new RequestForPosition(
                Shoulder.Position.RAISED, Elevator.Position.MID, Wrist.Position.MID_NODE);
    }

    public Request<Arm> requestForExtendedToHigh() {
        return new RequestForPosition(
                Shoulder.Position.RAISED, Elevator.Position.HIGH, Wrist.Position.HIGH_NODE);
    }

    public Request<Arm> requestForExtendedToHumanPlayerCone() {
        return new RequestForPosition(
                Shoulder.Position.RAISED,
                Elevator.Position.HUMAN_CONES,
                Wrist.Position.HUMAN_CONES);
    }

    public Request<Arm> requestForExtendedToHumanPlayerCube() {
        return new RequestForPosition(
                Shoulder.Position.RAISED,
                Elevator.Position.HUMAN_CUBES,
                Wrist.Position.HUMAN_CUBES);
    }

    public Request<Arm> requestForExtended(
            PlacementPosition position, GamePieceType gamePieceType) {
        return switch (position) {
            case NONE -> throw new IllegalArgumentException();
            case HIGH_NODE -> requestForExtendedToHigh();
            case HUMAN_PLAYER -> switch (gamePieceType) {
                case CONE -> requestForExtendedToHumanPlayerCone();
                case CUBE -> requestForExtendedToHumanPlayerCube();
            };
            case LOW_NODE -> requestForExtendedToLow();
            case MID_NODE -> requestForExtendedToMid();
        };
    }

    @Override
    protected Request<Arm> getIdleRequest() {
        return requestForHoldPosition();
    }

    @Override
    protected ArmStatus reportStatus() {
        return new ArmStatus();
    }
}

package com.team766.robot.gatorade.mechanisms;

import static com.team766.framework3.RulePersistence.ONCE_AND_HOLD;

import com.team766.framework3.Request;
import com.team766.framework3.Rule;
import com.team766.framework3.RuleBasedRequest;
import com.team766.framework3.Superstructure;
import com.team766.robot.gatorade.PlacementPosition;
import com.team766.robot.gatorade.mechanisms.Intake.GamePieceType;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class Arm extends Superstructure {
    private final Shoulder shoulder;
    private final Elevator elevator;
    private final Wrist wrist;

    public Arm() {
        this.shoulder = addMechanism(new Shoulder());
        this.elevator = addMechanism(new Elevator());
        this.wrist = addMechanism(new Wrist());
    }

    public Request<Arm> requestStop() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestStop()),
                        requestOfSubmechanism(elevator.requestStop()),
                        requestOfSubmechanism(wrist.requestStop())));
    }

    public Request<Arm> requestHoldPosition() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestHoldPosition()),
                        requestOfSubmechanism(elevator.requestHoldPosition()),
                        requestOfSubmechanism(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeShoulderUp() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestNudgeUp()),
                        requestOfSubmechanism(elevator.requestHoldPosition()),
                        requestOfSubmechanism(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeShoulderDown() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestNudgeDown()),
                        requestOfSubmechanism(elevator.requestHoldPosition()),
                        requestOfSubmechanism(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeElevatorUp() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestHoldPosition()),
                        requestOfSubmechanism(elevator.requestNudgeUp()),
                        requestOfSubmechanism(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeElevatorDown() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestHoldPosition()),
                        requestOfSubmechanism(elevator.requestNudgeDown()),
                        requestOfSubmechanism(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeWristUp() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestHoldPosition()),
                        requestOfSubmechanism(elevator.requestHoldPosition()),
                        requestOfSubmechanism(wrist.requestNudgeUp())));
    }

    public Request<Arm> requestNudgeWristDown() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestHoldPosition()),
                        requestOfSubmechanism(elevator.requestHoldPosition()),
                        requestOfSubmechanism(wrist.requestNudgeDown())));
    }

    public Request<Arm> requestPosition(
            double shoulderSetpoint, double elevatorSetpoint, double wristSetpoint) {
        final BooleanSupplier isRaisingShoulder =
                () -> shoulderSetpoint > shoulder.getStatus().angle();
        return startRequest(
                new RuleBasedRequest() {
                    {
                        addRule(
                                Rule.create(
                                                "Need to move elevator",
                                                () ->
                                                        !elevator.getStatus()
                                                                .isNearTo(elevatorSetpoint))
                                        .onTriggering(
                                                ONCE_AND_HOLD,
                                                Set.of(shoulder, wrist),
                                                () -> {
                                                    // If raising the shoulder, do that before the
                                                    // elevator (else, lower it after the elevator).
                                                    if (isRaisingShoulder.getAsBoolean()) {
                                                        shoulder.requestPosition(shoulderSetpoint);
                                                    } else {
                                                        shoulder.requestHoldPosition();
                                                    }
                                                    wrist.requestPosition(Wrist.Position.RETRACTED);
                                                })
                                        .whenTriggering(
                                                // Wait for wrist and possibly shoulder to move
                                                // before moving elevator.
                                                Rule.create(
                                                                "Wait for wrist and shoulder before moving elevator",
                                                                () ->
                                                                        wrist.getStatus()
                                                                                        .isNearTo(
                                                                                                wristSetpoint)
                                                                                || (isRaisingShoulder
                                                                                                .getAsBoolean()
                                                                                        && !shoulder.getStatus()
                                                                                                .isNearTo(
                                                                                                        shoulderSetpoint)))
                                                        .onTriggering(
                                                                ONCE_AND_HOLD,
                                                                Set.of(elevator),
                                                                () ->
                                                                        elevator
                                                                                .requestHoldPosition()),
                                                // Move the elevator until it gets near the target
                                                // position.
                                                Rule.create("Move elevator", UNCONDITIONAL)
                                                        .onTriggering(
                                                                ONCE_AND_HOLD,
                                                                Set.of(elevator),
                                                                () ->
                                                                        elevator.requestPosition(
                                                                                elevatorSetpoint))));

                        addRule(
                                "After elevator in position",
                                UNCONDITIONAL,
                                ONCE_AND_HOLD,
                                Set.of(shoulder, elevator, wrist),
                                () -> {
                                    // If lowering the shoulder, do that after the elevator.
                                    // Else, the shoulder was already moved into position, so keep
                                    // it there.
                                    shoulder.requestPosition(shoulderSetpoint);

                                    // The elevator is already in position, but keep it there.
                                    elevator.requestPosition(elevatorSetpoint);

                                    // Lastly, move the wrist to its target angle.
                                    wrist.requestPosition(wristSetpoint);
                                });
                    }

                    @Override
                    protected boolean isDone() {
                        return shoulder.getStatus().isNearTo(shoulderSetpoint)
                                && elevator.getStatus().isNearTo(elevatorSetpoint)
                                && wrist.getStatus().isNearTo(wristSetpoint);
                    }
                });
    }

    public Request<Arm> requestRetracted() {
        return requestPosition(
                Shoulder.Position.BOTTOM, Elevator.Position.RETRACTED, Wrist.Position.RETRACTED);
    }

    public Request<Arm> requestExtendedToLow() {
        return requestPosition(
                Shoulder.Position.FLOOR, Elevator.Position.LOW, Wrist.Position.LEVEL);
    }

    public Request<Arm> requestExtendedToMid() {
        return requestPosition(
                Shoulder.Position.RAISED, Elevator.Position.MID, Wrist.Position.MID_NODE);
    }

    public Request<Arm> requestExtendedToHigh() {
        return requestPosition(
                Shoulder.Position.RAISED, Elevator.Position.HIGH, Wrist.Position.HIGH_NODE);
    }

    public Request<Arm> requestExtendedToHumanPlayerCone() {
        return requestPosition(
                Shoulder.Position.RAISED,
                Elevator.Position.HUMAN_CONES,
                Wrist.Position.HUMAN_CONES);
    }

    public Request<Arm> requestExtendedToHumanPlayerCube() {
        return requestPosition(
                Shoulder.Position.RAISED,
                Elevator.Position.HUMAN_CUBES,
                Wrist.Position.HUMAN_CUBES);
    }

    public Request<Arm> requestExtended(PlacementPosition position, GamePieceType gamePieceType) {
        return switch (position) {
            case NONE -> throw new IllegalArgumentException();
            case HIGH_NODE -> requestExtendedToHigh();
            case HUMAN_PLAYER -> switch (gamePieceType) {
                case CONE -> requestExtendedToHumanPlayerCone();
                case CUBE -> requestExtendedToHumanPlayerCube();
            };
            case LOW_NODE -> requestExtendedToLow();
            case MID_NODE -> requestExtendedToMid();
        };
    }

    @Override
    protected Request<Arm> startIdleRequest() {
        return requestHoldPosition();
    }
}

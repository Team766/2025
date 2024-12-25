package com.team766.robot.gatorade.mechanisms;

import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.framework3.Superstructure;
import com.team766.robot.gatorade.PlacementPosition;
import com.team766.robot.gatorade.mechanisms.Intake.GamePieceType;

public class Arm extends Superstructure<Arm.ArmStatus> {
    public record ArmStatus() implements Status {}

    private final Shoulder shoulder;
    private final Elevator elevator;
    private final Wrist wrist;

    public Arm() {
        this.shoulder = addMechanism(new Shoulder());
        this.elevator = addMechanism(new Elevator());
        this.wrist = addMechanism(new Wrist());
    }

    public Request<Arm> requestStop() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestStop()),
                        submechanismRequest(elevator.requestStop()),
                        submechanismRequest(wrist.requestStop())));
    }

    public Request<Arm> requestHoldPosition() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestHoldPosition()),
                        submechanismRequest(elevator.requestHoldPosition()),
                        submechanismRequest(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeShoulderUp() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestNudgeUp()),
                        submechanismRequest(elevator.requestHoldPosition()),
                        submechanismRequest(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeShoulderDown() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestNudgeDown()),
                        submechanismRequest(elevator.requestHoldPosition()),
                        submechanismRequest(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeElevatorUp() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestHoldPosition()),
                        submechanismRequest(elevator.requestNudgeUp()),
                        submechanismRequest(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeElevatorDown() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestHoldPosition()),
                        submechanismRequest(elevator.requestNudgeDown()),
                        submechanismRequest(wrist.requestHoldPosition())));
    }

    public Request<Arm> requestNudgeWristUp() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestHoldPosition()),
                        submechanismRequest(elevator.requestHoldPosition()),
                        submechanismRequest(wrist.requestNudgeUp())));
    }

    public Request<Arm> requestNudgeWristDown() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestHoldPosition()),
                        submechanismRequest(elevator.requestHoldPosition()),
                        submechanismRequest(wrist.requestNudgeDown())));
    }

    public Request<Arm> requestPosition(
            double shoulderSetpoint, double elevatorSetpoint, double wristSetpoint) {
        return setRequest(
                () -> {
                    if (elevator.getStatus().isNearTo(elevatorSetpoint)) {
                        // If lowering the shoulder, do that after
                        // the elevator.
                        shoulder.requestPosition(shoulderSetpoint);

                        elevator.requestPosition(elevatorSetpoint);

                        // Lastly, move the wrist.
                        wrist.requestPosition(wristSetpoint);

                        return shoulder.getStatus().isNearTo(shoulderSetpoint)
                                && wrist.getStatus().isNearTo(wristSetpoint);
                    }

                    final boolean isRaisingShoulder =
                            shoulderSetpoint > shoulder.getStatus().angle();

                    // Prepare to move elevator
                    if (!wrist.getStatus().isNearTo(Wrist.Position.RETRACTED)
                            ||
                            // If raising the shoulder, do
                            // that before the elevator
                            // (else, lower it after the
                            // elevator).
                            (isRaisingShoulder
                                    && !shoulder.getStatus().isNearTo(shoulderSetpoint))) {
                        if (isRaisingShoulder) {
                            shoulder.requestPosition(shoulderSetpoint);
                        } else {
                            shoulder.requestHoldPosition();
                        }

                        elevator.requestHoldPosition();

                        wrist.requestPosition(Wrist.Position.RETRACTED);

                        return false;
                    }

                    // Moving elevator
                    {
                        if (isRaisingShoulder) {
                            shoulder.requestPosition(shoulderSetpoint);
                        } else {
                            shoulder.requestHoldPosition();
                        }

                        // Move the elevator until it gets near the
                        // target position.
                        elevator.requestPosition(elevatorSetpoint);

                        wrist.requestPosition(Wrist.Position.RETRACTED);

                        return false;
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
    protected Request<Arm> applyIdleRequest() {
        return requestHoldPosition();
    }

    @Override
    protected ArmStatus reportStatus() {
        return new ArmStatus();
    }
}

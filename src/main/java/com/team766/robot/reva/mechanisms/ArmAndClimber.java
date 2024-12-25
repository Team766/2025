package com.team766.robot.reva.mechanisms;

import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.framework3.Superstructure;
import java.util.function.Supplier;

public class ArmAndClimber extends Superstructure<ArmAndClimber.SuperstructureStatus> {
    public record SuperstructureStatus() implements Status {}

    private Request<ArmAndClimber> requestShoulder(Supplier<Request<Shoulder>> shoulderRequest) {
        return setRequest(
                requestAllOf(
                        submechanismRequest(climber.requestPosition(Climber.Position.BOTTOM)),
                        () -> {
                            final var climberStatus = climber.getStatus();
                            final boolean climberIsBelowArm =
                                    climberStatus.heightLeft() < Climber.Position.BELOW_ARM
                                            && climberStatus.heightRight()
                                                    < Climber.Position.BELOW_ARM;
                            if (climberIsBelowArm) {
                                return shoulderRequest.get().isDone();
                            } else {
                                return shoulder.requestHoldPosition().isDone();
                            }
                        }));
    }

    public Request<ArmAndClimber> requestShoulderPosition(double targetPosition) {
        return requestShoulder(() -> shoulder.requestPosition(targetPosition));
    }

    public Request<ArmAndClimber> requestShoulderNudgeUp() {
        return requestShoulder(shoulder::requestNudgeUp);
    }

    public Request<ArmAndClimber> requestShoulderNudgeDown() {
        return requestShoulder(shoulder::requestNudgeDown);
    }

    public Request<ArmAndClimber> requestClimberStop() {
        return setRequest(submechanismRequest(climber.requestStop()));
    }

    public Request<ArmAndClimber> requestClimberMotorPowers(
            double powerLeft, double powerRight, boolean overrideSoftLimits) {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestPosition(Shoulder.Position.TOP)),
                        () -> {
                            if (shoulder.getStatus().isNearTo(Shoulder.Position.TOP)) {
                                return climber.requestMotorPowers(
                                                powerLeft, powerRight, overrideSoftLimits)
                                        .isDone();
                            } else {
                                return climber.requestStop().isDone();
                            }
                        }));
    }

    public Request<ArmAndClimber> requestClimberPosition(double targetHeight) {
        if (targetHeight < Climber.Position.BELOW_ARM) {
            return setRequest(submechanismRequest(climber.requestPosition(targetHeight)));
        } else {
            return setRequest(
                    requestAllOf(
                            submechanismRequest(shoulder.requestPosition(Shoulder.Position.TOP)),
                            () -> {
                                if (shoulder.getStatus().isNearTo(Shoulder.Position.TOP)) {
                                    return climber.requestPosition(targetHeight).isDone();
                                } else {
                                    return climber.requestStop().isDone();
                                }
                            }));
        }
    }

    public Request<ArmAndClimber> requestStop() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestStop()),
                        submechanismRequest(climber.requestStop())));
    }

    public Request<ArmAndClimber> requestHoldPosition() {
        return setRequest(
                requestAllOf(
                        submechanismRequest(shoulder.requestHoldPosition()),
                        submechanismRequest(climber.requestStop())));
    }

    private final Shoulder shoulder;
    private final Climber climber;

    public ArmAndClimber() {
        this(new Shoulder(), new Climber());
    }

    public ArmAndClimber(Shoulder shoulder, Climber climber) {
        this.shoulder = addMechanism(shoulder);
        this.climber = addMechanism(climber);
    }

    public void resetShoulder() {
        checkContextReservation();
        shoulder.reset();
    }

    public void resetClimberPositions() {
        checkContextReservation();
        climber.resetLeftPosition();
        climber.resetRightPosition();
    }

    @Override
    protected SuperstructureStatus reportStatus() {
        return new SuperstructureStatus();
    }
}

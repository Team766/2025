package com.team766.robot.reva.mechanisms;

import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.framework3.Superstructure;

public class ArmAndClimber extends Superstructure<ArmAndClimber.SuperstructureStatus> {
    public record SuperstructureStatus() implements Status {}

    public Request<ArmAndClimber> requestShoulderPosition(double targetPosition) {
        return startRequest(
                () -> {
                    climber.requestPosition(Climber.Position.BOTTOM);

                    final var climberStatus = climber.getStatus();
                    final boolean climberIsBelowArm =
                            climberStatus.heightLeft() < Climber.Position.BELOW_ARM
                                    && climberStatus.heightRight() < Climber.Position.BELOW_ARM;
                    if (climberIsBelowArm) {
                        var shoulderRequest = shoulder.requestPosition(targetPosition);
                        return shoulderRequest.isDone();
                    } else {
                        shoulder.requestHoldPosition();
                        return false;
                    }
                });
    }

    public Request<ArmAndClimber> requestShoulderNudgeUp() {
        return requestShoulderPosition(shoulder.getNudgeUpPosition());
    }

    public Request<ArmAndClimber> requestShoulderNudgeDown() {
        return requestShoulderPosition(shoulder.getNudgeDownPosition());
    }

    public Request<ArmAndClimber> requestClimberStop() {
        return startRequest(requestOfSubmechanism(climber.requestStop()));
    }

    public Request<ArmAndClimber> requestClimberMotorPowers(
            double powerLeft, double powerRight, boolean overrideSoftLimits) {
        return startRequest(
                () -> {
                    var shoulderRequest = shoulder.requestPosition(Shoulder.Position.TOP);
                    if (shoulderRequest.isDone()) {
                        var climberRequest =
                                climber.requestMotorPowers(
                                        powerLeft, powerRight, overrideSoftLimits);
                        return climberRequest.isDone();
                    } else {
                        climber.requestStop();
                        return false;
                    }
                });
    }

    public Request<ArmAndClimber> requestClimberPosition(double targetHeight) {
        if (targetHeight < Climber.Position.BELOW_ARM) {
            return startRequest(requestOfSubmechanism(climber.requestPosition(targetHeight)));
        } else {
            return startRequest(
                    () -> {
                        var shoulderRequest = shoulder.requestPosition(Shoulder.Position.TOP);
                        if (shoulderRequest.isDone()) {
                            var climberRequest = climber.requestPosition(targetHeight);
                            return climberRequest.isDone();
                        } else {
                            climber.requestStop();
                            return false;
                        }
                    });
        }
    }

    public Request<ArmAndClimber> requestStop() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestStop()),
                        requestOfSubmechanism(climber.requestStop())));
    }

    public Request<ArmAndClimber> requestHoldPosition() {
        return startRequest(
                requestAllOf(
                        requestOfSubmechanism(shoulder.requestHoldPosition()),
                        requestOfSubmechanism(climber.requestStop())));
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

package com.team766.robot.reva.mechanisms;

import com.team766.framework3.MultiRequest;
import com.team766.framework3.Request;
import com.team766.framework3.RequestForSubmechanism;
import com.team766.framework3.Status;
import com.team766.framework3.Superstructure;

public class ArmAndClimber
        extends Superstructure<ArmAndClimber, ArmAndClimber.SuperstructureStatus> {
    public record SuperstructureStatus() implements Status {}

    // NOTE: This request type is private because we don't want to expose the ability
    // to send arbitrary requests to the individual mechanisms.
    private Request<ArmAndClimber> simpleRequest(
            Request<Shoulder> shoulderRequest, Request<Climber> climberRequest) {
        return new MultiRequest<ArmAndClimber>(
                new RequestForSubmechanism<>(shoulder, shoulderRequest),
                new RequestForSubmechanism<>(climber, climberRequest));
    }

    public Request<ArmAndClimber> requestForShoulder(Request<Shoulder> shoulderRequest) {
        return new MultiRequest<ArmAndClimber>(
                new RequestForSubmechanism<>(
                        climber, climber.requestForPosition(Climber.Position.BOTTOM)));

        final var climberStatus = climber.getStatus();
        final boolean climberIsBelowArm =
                climberStatus.heightLeft() < Climber.Position.BELOW_ARM
                        && climberStatus.heightRight() < Climber.Position.BELOW_ARM;
        if (climberIsBelowArm) {
            shoulder.setRequest(shoulderRequest);
        } else {
            shoulder.setRequest(shoulder.requestForHoldPosition());
        }
    }

    public Request<ArmAndClimber> requestForClimber(Request<Climber> climberRequest) {
        final boolean climberRequestIsInnocuous =
                switch (g.climberRequest()) {
                    case Climber.Stop c -> true;
                    case Climber.MotorPowers c -> false;
                    case Climber.MoveToPosition c -> (c.height()
                            < Climber.MoveToPosition.BELOW_ARM.height());
                };

        if (climberRequestIsInnocuous) {
            climber.setRequest(climberRequest);
        } else {
            shoulder.setRequest(shoulder.requestForPosition(Shoulder.Position.TOP));
            if (shoulder.getStatus().isNearTo(Shoulder.Position.TOP)) {
                climber.setRequest(climberRequest);
            } else {
                climber.setRequest(climber.requestForStop());
            }
        }
    }

    public Request<ArmAndClimber> requestForStop() {
        return simpleRequest(shoulder.requestForStop(), climber.requestForStop());
    }

    public Request<ArmAndClimber> requestForHoldPosition() {
        return simpleRequest(shoulder.requestForHoldPosition(), climber.requestForStop());
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

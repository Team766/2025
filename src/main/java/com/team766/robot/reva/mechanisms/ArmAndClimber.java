package com.team766.robot.reva.mechanisms;

import static com.team766.framework3.RulePersistence.ONCE_AND_HOLD;

import com.team766.framework3.Request;
import com.team766.framework3.RuleBasedRequest;
import com.team766.framework3.Superstructure;
import java.util.Set;

public class ArmAndClimber extends Superstructure {
    public Request<ArmAndClimber> requestShoulderPosition(double targetPosition) {
        return startRequest(
                new RuleBasedRequest() {
                    {
                        addRule(
                                "Move climber out of conflict",
                                () ->
                                        climber.getStatus().heightLeft()
                                                        > Climber.Position.BELOW_ARM
                                                || climber.getStatus().heightRight()
                                                        > Climber.Position.BELOW_ARM,
                                ONCE_AND_HOLD,
                                Set.of(climber, shoulder),
                                () -> {
                                    shoulder.requestHoldPosition();
                                    climber.requestPosition(Climber.Position.BOTTOM);
                                });

                        addRule(
                                "Move shoulder when available",
                                UNCONDITIONAL,
                                shoulder,
                                () -> shoulder.requestPosition(targetPosition));
                    }

                    @Override
                    protected boolean isDone() {
                        return shoulder.getStatus().isNearTo(targetPosition);
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
                new RuleBasedRequest() {
                    {
                        addRule(
                                "Move shoulder out of conflict",
                                () -> !shoulder.getStatus().isNearTo(Shoulder.Position.TOP),
                                ONCE_AND_HOLD,
                                Set.of(climber, shoulder),
                                () -> {
                                    climber.requestStop();
                                    shoulder.requestPosition(Shoulder.Position.TOP);
                                });

                        addRule(
                                "Move climber when available",
                                UNCONDITIONAL,
                                climber,
                                () ->
                                        climber.requestMotorPowers(
                                                powerLeft, powerRight, overrideSoftLimits));
                    }

                    @Override
                    protected boolean isDone() {}
                });
    }

    public Request<ArmAndClimber> requestClimberPosition(double targetHeight) {
        return startRequest(
                new RuleBasedRequest() {
                    {
                        addRule(
                                "Move shoulder out of conflict",
                                () ->
                                        targetHeight > Climber.Position.BELOW_ARM
                                                && !shoulder.getStatus()
                                                        .isNearTo(Shoulder.Position.TOP),
                                ONCE_AND_HOLD,
                                Set.of(climber, shoulder),
                                () -> {
                                    climber.requestStop();
                                    shoulder.requestPosition(Shoulder.Position.TOP);
                                });

                        addRule(
                                "Move climber when available",
                                UNCONDITIONAL,
                                climber,
                                () -> climber.requestPosition(targetHeight));
                    }

                    @Override
                    protected boolean isDone() {
                        return climber.getStatus().isLeftNear(targetHeight)
                                && climber.getStatus().isRightNear(targetHeight);
                    }
                });
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
}

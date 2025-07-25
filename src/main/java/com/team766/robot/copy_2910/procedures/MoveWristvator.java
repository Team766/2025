package com.team766.robot.copy_2910.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.copy_2910.mechanisms.Elevator;
import com.team766.robot.copy_2910.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.copy_2910.mechanisms.Shoulder;
import com.team766.robot.copy_2910.mechanisms.Shoulder.ShoulderPosition;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import com.team766.robot.copy_2910.mechanisms.Wrist.WristPosition;

public class MoveWristvator extends Procedure {
    private final Shoulder shoulder;
    private final Elevator elevator;
    private final Wrist wrist;
    private final ShoulderPosition shoulderSetpoint;
    private final ElevatorPosition elevatorSetpoint;
    private final WristPosition wristSetpoint;

    public MoveWristvator(
            Shoulder shoulder,
            Elevator elevator,
            Wrist wrist,
            ShoulderPosition shoulderSetpoint_,
            ElevatorPosition elevatorSetpoint_,
            WristPosition wristSetpoint_) {
        this.shoulder = reserve(shoulder);
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.shoulderSetpoint = shoulderSetpoint_;
        this.elevatorSetpoint = elevatorSetpoint_;
        this.wristSetpoint = wristSetpoint_;
    }

    @Override
    public final void run(Context context) {
        // Always retract the wrist before moving the elevator.
        // It might already be retracted, so it's possible that this step finishes instantaneously.
        wrist.setPosition(WristPosition.STOW);
        // If raising the shoulder, do that before the elevator (else, lower it after the elevator).
        if (shoulderSetpoint.getPosition()
                > getStatusOrThrow(Shoulder.ShoulderStatus.class).position()) {
            shoulder.setPosition(shoulderSetpoint);
            waitForStatusMatching(
                    context, Shoulder.ShoulderStatus.class, s -> s.isNearTo(shoulderSetpoint));
        }
        waitForStatusMatching(
                context, Wrist.WristStatus.class, s -> s.isNearTo(WristPosition.STOW));

        // Move the elevator. Wait until it gets near the target position.
        elevator.setPosition(elevatorSetpoint);
        waitForStatusMatching(context, Elevator.ElevatorStatus.class, s -> s.isAtHeight());

        // If lowering the shoulder, do that after the elevator.
        if (shoulderSetpoint.getPosition()
                < getStatusOrThrow(Shoulder.ShoulderStatus.class).position()) {
            shoulder.setPosition(shoulderSetpoint);
        }

        // Lastly, move the wrist.
        wrist.setPosition(wristSetpoint);
        waitForStatusMatching(context, Wrist.WristStatus.class, s -> s.isNearTo(wristSetpoint));
        waitForStatusMatching(
                context, Shoulder.ShoulderStatus.class, s -> s.isNearTo(shoulderSetpoint));
    }
}

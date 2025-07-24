package com.team766.robot.copy_2910.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.copy_2910.mechanisms.Intake;
import java.util.Optional;

public class IntakeCoral extends Procedure {

    private Intake intake;

    public IntakeCoral(Intake intake2) {
        intake = reserve(intake2);
    }

    @Override
    public void run(Context context) {
        Optional<Intake.IntakeStatus> status = getStatus(Intake.IntakeStatus.class);
        if (status.isEmpty()) {
            log("No intake status");
            return;
        }
        log(
                "Intake Status: leftDistance = "
                        + status.get().getLeftDistance()
                        + "Back center:"
                        + status.get().getBackCenterDistance());
        while (!status.get().hasCoralInBackCenter()) {
            context.yield();
            intake.turnAlgaePositive();
            status = getStatus(Intake.IntakeStatus.class);
            boolean hasCoralInLeft = status.get().hasCoralInLeft();
            boolean hasCoralInRight = status.get().hasCoralInRight();
            boolean hasCoralInFrontCenter = status.get().hasCoralInFrontCenter();

            /*
             * Case 1: Coral is near the intake and is alligned horizontally
             *
             * |                coral                 |
             *   |-------------intake------------|
             *
             * We need to move the coral to the right so that it can get intaked from the center.
             * Thus, we need to turn both motors clockwise–to the right, or in the positive direction.
             */
            if (hasCoralInLeft && hasCoralInRight && hasCoralInFrontCenter) {
                intake.turnLeftPositive();
                intake.turnRightPositive();
                continue;
            }

            /*
             * Case 2: Coral is only in the right side of the intake
             * |       coral       |
             *  |-----------------------intake--------------------|
             *
             * We need to move the coral to the right so that it can get intaked from the center.
             * Thus, we need to turn the right motor clockwise–to the right, or in the positive direction.
             * And, we should turn the left motor anticlockwise–to the left, or in the negative direction, to push it in.
             */
            /*
             * Case 3: Coral is only in the left side of the intake
             *                                    |       coral       |
             * |-----------------------intake--------------------|
             * * We need to move the coral to the left so that it can get intaked from the center.
             * Thus, we need to turn the left motor anticlockwise, to the left, or in the negative direction.
             * And, we should turn the right motor clockwise, to the right, or in the positive direction, to push it in.
             * Crazy enough, this is the same as situation two!
             */
            /*
             * Case 4: Coral is in the front center of the intake
             *              |       coral       |

             *
             * We just need to move the coral inwards, so spin the right motor clockwise, to the right, or in the positive direction,
             * and the left motor anticlockwise, to the left, or in the negative direction.
             * This is the same as situation two and three!
             */
            else {
                intake.turnLeftNegative();
                intake.turnRightPositive();
            }
        }
        // Once the coral is in the back center, we stop the intake motors.
        intake.turnLeftPositive();
        intake.turnRightNegative();
        context.waitForSeconds(0.05);
        intake.stop();
    }
}

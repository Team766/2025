package com.team766.robot.gatorade.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.gatorade.GamePieceType;
import com.team766.robot.gatorade.mechanisms.Elevator;
import com.team766.robot.gatorade.mechanisms.Intake;
import com.team766.robot.gatorade.mechanisms.Shoulder;
import com.team766.robot.gatorade.mechanisms.Wrist;

public class ScoreHigh extends Procedure {
    private final GamePieceType type;
    private final Shoulder shoulder;
    private final Elevator elevator;
    private final Wrist wrist;
    private final Intake intake;

    public ScoreHigh(
            GamePieceType type, Shoulder shoulder, Elevator elevator, Wrist wrist, Intake intake) {
        this.type = type;
        this.shoulder = reserve(shoulder);
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        context.runSync(new ExtendWristvatorToHigh(shoulder, elevator, wrist));
        intake.out(type);
        context.waitForSeconds(1);
        intake.stop();
    }
}

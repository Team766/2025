package com.team766.robot.gatorade.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.gatorade.GamePieceType;
import com.team766.robot.gatorade.mechanisms.Intake;

public class IntakeIdle extends Procedure {
    private final GamePieceType gamePieceType;
    private final Intake intake;

    public IntakeIdle(GamePieceType gamePieceType, Intake intake) {
        this.gamePieceType = gamePieceType;
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        intake.idle(gamePieceType);
    }
}

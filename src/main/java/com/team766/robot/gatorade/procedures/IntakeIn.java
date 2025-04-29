package com.team766.robot.gatorade.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.gatorade.GamePieceType;
import com.team766.robot.gatorade.mechanisms.Intake;

public class IntakeIn extends Procedure {
    private final GamePieceType gamePieceType;
    private final Intake intake;

    public IntakeIn(GamePieceType gamePieceType, Intake intake) {
        this.gamePieceType = gamePieceType;
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        intake.in(gamePieceType);
    }
}

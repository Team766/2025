package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.ONCE_AND_HOLD;

import com.team766.hal.JoystickReader;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;

public class DriverOI_GamePad extends com.team766.robot.common.DriverOI_GamePad {
    public DriverOI_GamePad(JoystickReader gamePad, SwerveDrive drive, CoralIntake coralIntake) {
        super(gamePad, drive);
        addRule(
                "Outtake Coral",
                gamePad.whenButton(InputConstants.BUTTON_CORAL_PLACE),
                ONCE_AND_HOLD,
                coralIntake,
                () -> {
                    coralIntake.out();
                });
    }
}

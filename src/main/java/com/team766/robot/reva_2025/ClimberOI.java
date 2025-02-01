package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.Climber;

public class ClimberOI extends RuleGroup {
    public ClimberOI(JoystickReader gamePad, Climber climber) {
        addRule(
                        "Climber up",
                        gamePad.whenButton(InputConstants.GAMEPAD_BUTTON_CLIMB),
                        ONCE_AND_HOLD,
                        climber,
                        () -> {
                            climber.climbUp();
                        })
                .withFinishedTriggeringProcedure(
                        climber,
                        () -> {
                            climber.climbDown();
                        });
    }
}

package com.team766.robot.reva_2025.OI;

import static com.team766.framework3.RulePersistence.ONCE_AND_HOLD;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.Climber;

public class ClimberOI extends RuleGroup {
    public ClimberOI(JoystickReader gamePad, Climber climber) {
        addRule(
                "Climber Up",
                () -> gamePad.getPOV() == InputConstants.DPAD_UP,
                ONCE_AND_HOLD,
                climber,
                () -> {
                    climber.climberUp();
                });

        addRule(
                "Climber Down",
                () -> gamePad.getPOV() == InputConstants.DPAD_DOWN,
                ONCE_AND_HOLD,
                climber,
                () -> {
                    climber.climberDown();
                });
    }
}

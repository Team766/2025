package com.team766.robot.reva_2025.OI;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class WristOI extends RuleGroup {
    public WristOI(JoystickReader gamePad, Wrist wrist) {
        addRule(
                "Pickup Coral",
                gamePad.whenButton(InputConstants.GAMEPAD_X_BUTTON),
                ONCE,
                wrist,
                () -> {
                    wrist.setPosition(Wrist.WristPosition.WRIST_BOTTOM);
                });

        addRule(
                "Place Coral",
                gamePad.whenButton(InputConstants.GAMEPAD_B_BUTTON),
                ONCE,
                wrist,
                () -> {
                    wrist.setPosition(Wrist.WristPosition.WRIST_TOP);
                });
    }
}

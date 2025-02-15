package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class WristOI extends RuleGroup {
    public WristOI(JoystickReader gamePad, Wrist wrist) {
        addRule(
                "Pickup Coral",
                gamePad.whenButton(InputConstants.GAMEPAD_BUTTON_A),
                ONCE,
                wrist,
                () -> {
                    wrist.setAngle(Wrist.WristPosition.PICKUP_CORAL);
                });

        addRule(
                "Place Coral",
                gamePad.whenButton(InputConstants.GAMEPAD_BUTTON_Y),
                ONCE,
                wrist,
                () -> {
                    wrist.setAngle(Wrist.WristPosition.PLACE_CORAL);
                });
    }
}

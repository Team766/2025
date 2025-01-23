package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class WristOI extends RuleGroup {
    public WristOI (
        JoystickReader gamePad,
        Wrist wrist ){
            addRule(
                "WristX",
                gamePad.isAxisMoved(InputConstants.GAMEPAD_RIGHT_THUMBSTICK_X_AXIS),
                REPEATEDLY,
                wrist,
                () -> {
                    wrist.WristX();
                }
            );

            addRule(
                "WristY",
                gamePad.isAxisMoved(InputConstants.GAMEPAD_RIGHT_THUMBSTICK_Y_AXIS),
                REPEATEDLY,
                wrist,
                () -> {
                    wrist.WristY();
                }
            );
        }
    
}

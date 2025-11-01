package com.team766.robot.ArthurDoering.OI;

import static com.team766.framework.RulePersistence.*;
import java.util.Set;
import com.ctre.phoenix.led.*;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.LEDString;

public class CapstoneLights extends RuleGroup {
    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, 512);

    public CapstoneLights() {
        final JoystickReader gamepad = RobotProvider.instance.getJoystick(1);
        addRule(
                "Cone",
                gamepad.whenButton(InputConstants.GAMEPAD_DPAD_LEFT),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    segment.setColor(ColorConstants.YELLOW);
                });
        addRule(
                "Cube",
                gamepad.whenButton(InputConstants.GAMEPAD_DPAD_RIGHT),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    segment.setColor(ColorConstants.PURPLE);
                });
        addRule(
                "Defense",
                gamepad.whenButton(InputConstants.GAMEPAD_DPAD_DOWN),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    Animation rainbowAnim = new RainbowAnimation();
                    segment.animate(rainbowAnim);
                });
    }
}

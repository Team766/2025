package com.team766.robot.mark.CANdle;

import static com.team766.framework.RulePersistence.*;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.LEDString;
import java.util.Set;

public class CapstoneLights extends RuleGroup {
    private final LEDString ledString = new LEDString("LEDs");
    private final LEDString.Segment ledStringSegment = ledString.makeSegment(0, 7);

    public CapstoneLights() {
        final Animation rainbowAnim = new RainbowAnimation();
        final JoystickReader gamepad = RobotProvider.instance.getJoystick(1);

        addRule(
                "Yellow Lights for Cone",
                gamepad.whenButton(InputConstants.GAMEPAD_DPAD_LEFT),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    ledStringSegment.setColor(ColorConstants.YELLOW);
                });

        addRule(
                "Purple Lights for Cube",
                gamepad.whenButton(InputConstants.GAMEPAD_DPAD_DOWN),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    ledStringSegment.setColor(ColorConstants.PURPLE);
                });

        addRule(
                "Rainbow Lights for Defense",
                gamepad.whenButton(InputConstants.GAMEPAD_DPAD_UP),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    ledStringSegment.animate(rainbowAnim);
                });
    }
}

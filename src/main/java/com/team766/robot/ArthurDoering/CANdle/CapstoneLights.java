package com.team766.robot.ArthurDoering.CANdle;

import static com.team766.framework.RulePersistence.*;

import com.ctre.phoenix.led.*;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.LEDString;
import java.util.Set;

public class CapstoneLights extends RuleGroup {
    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, 512);

    public CapstoneLights() {
        final JoystickReader gamepad = RobotProvider.instance.getJoystick(1);
        addRule(
                "Cone",
                gamepad.whenButton(InputConstants.GAMEPAD_A_BUTTON),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    segment.setColor(ColorConstants.YELLOW);
                });
        addRule(
                "Cube",
                gamepad.whenButton(InputConstants.GAMEPAD_B_BUTTON),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    segment.setColor(ColorConstants.PURPLE);
                });
        addRule(
                "Defense",
                gamepad.whenButton(InputConstants.GAMEPAD_Y_BUTTON),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {
                    Animation rainbowAnim = new RainbowAnimation();
                    segment.animate(rainbowAnim);
                });
    }
}

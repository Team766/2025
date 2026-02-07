package com.team766.robot.filip;
import java.util.Set;
import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.common.constants.ColorConstants;
import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.robot.common.constants.InputConstants;

public class LightsCANdle extends RuleGroup{
    private final LEDString lights = new LEDString("lights");
    private final LEDString.Segment lightStrip = lights.makeSegment(0,7);

    public LightsCANdle () {
        final Animation rainbow = new RainbowAnimation();
        final JoystickReader joystick = RobotProvider.instance.getJoystick(0);

        addRule("Yellow for Cone",
            joystick.whenButton(InputConstants.GAMEPAD_A_BUTTON),
            ONCE,
            Set.of(lights),
            () -> {lightStrip.setColor(ColorConstants.YELLOW);}
        );

        addRule("Purple for Cube",
            joystick.whenButton(InputConstants.GAMEPAD_B_BUTTON),
            ONCE,
            Set.of(lights),
            () -> {lightStrip.setColor(ColorConstants.PURPLE);}
        );

        addRule("Rainbow for Defense",
            joystick.whenButton(InputConstants.GAMEPAD_X_BUTTON),
            ONCE,
            Set.of(lights),
            () -> {lightStrip.animate(rainbow);}
        );
    }
}
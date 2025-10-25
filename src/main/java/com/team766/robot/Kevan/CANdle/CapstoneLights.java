package com.team766.robot.Kevan.CANdle;

import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.FireAnimation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework.Conditions;
import com.team766.framework.RuleGroup;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.Joystick;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.reva_2025.Lights;
import com.team766.robot.common.constants.InputConstants;
import static com.team766.framework.RulePersistence.*;
import com.team766.framework.RuleEngine;



public class CapstoneLights extends RuleGroup {
    
    private final LEDString ledString = new LEDString("leds");
    
    public CapstoneLights() {
        final JoystickReader button1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader button2 = RobotProvider.instance.getJoystick(2);
        final JoystickReader button3 = RobotProvider.instance.getJoystick(3);

        addRule("Yellow Lights for Cone",
                button1.getButton(1),
                ONCE_AND_HOLD,
                ledString,
                () -> {ledString.setColor(ColorConstants.YELLOW);});

        addRule("Purple Lights for Cube",
                button2.getButton(2),
                ONCE_AND_HOLD,
                ledString,
                () -> {ledString.setColor(ColorConstants.PURPLE);});

        addRule("Rainbow Lights for Defense",
                button3.getButton(3),
                ONCE_AND_HOLD,
                ledString,
                () -> {Animation rainbowAnim = new RainbowAnimation(); ledString.animate(rainbowAnim);});
    }
}
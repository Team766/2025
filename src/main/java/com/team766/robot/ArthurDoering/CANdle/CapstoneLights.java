package com.team766.robot.ArthurDoering.CANdle;

import static com.team766.framework.RulePersistence.*;
import java.util.Set;
import com.ctre.phoenix.led.*;
import com.ctre.phoenix.led.CANdle.LEDStripType;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.example.Lights;
import com.team766.framework.RuleEngine;
import com.team766.framework.RuleGroup;
import com.team766.framework.RulePersistence;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;

public class CapstoneLights extends RuleGroup{
    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, 512);


    public CapstoneLights() {
        final JoystickReader buttonOne = RobotProvider.instance.getJoystick(1);
        final JoystickReader buttonTwo = RobotProvider.instance.getJoystick(2);
        final JoystickReader buttonThr = RobotProvider.instance.getJoystick(3);
        addRule("Cone",
        buttonOne.getButton(1),
        ONCE_AND_HOLD,
        segment,
        () -> {segment.setColor(ColorConstants.YELLOW);}
        );
        addRule("Cube",
        buttonTwo.getButton(2),
        ONCE_AND_HOLD,
        segment,
        () -> {segment.setColor(ColorConstants.PURPLE);}
        );
        addRule("Defense",
        buttonThr.getButton(3),
        ONCE_AND_HOLD,
        segment,
        () -> {Animation rainbowAnim = new RainbowAnimation();
                    segment.animate(rainbowAnim);}
        );
    }
}

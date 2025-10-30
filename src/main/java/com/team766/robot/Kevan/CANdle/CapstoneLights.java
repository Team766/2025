package com.team766.robot.Kevan.CANdle;
import java.util.Set;
import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.common.constants.ColorConstants;
import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.RainbowAnimation;

public class CapstoneLights extends RuleGroup {
    
    private final LEDString ledString  = new LEDString("LEDs");
    private final LEDString.Segment ledStringSegment = ledString.makeSegment(0, 7);
    
    public CapstoneLights() {
        
        final Animation rainbowAnim = new RainbowAnimation();
        final JoystickReader gamePad = RobotProvider.instance.getJoystick(1);


        addRule("Yellow Lights for Cone Right",
                gamePad.whenButton(InputConstants.GAMEPAD_DPAD_RIGHT),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {ledStringSegment.setColor(ColorConstants.YELLOW);});

        addRule("Purple Lights for Cube Left",
                gamePad.whenButton(InputConstants.GAMEPAD_DPAD_LEFT),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {ledStringSegment.setColor(ColorConstants.PURPLE);});

        addRule("Rainbow Lights for Defense Down",
                gamePad.whenButton(InputConstants.GAMEPAD_DPAD_DOWN),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {ledStringSegment.animate(rainbowAnim);});
    }
}
package com.team766.robot.Kevan.CANdle;
import java.util.Set;
import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.common.constants.ColorConstants;
import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.RainbowAnimation;

public class CapstoneLights extends RuleGroup {
    
    private final LEDString ledString  = new LEDString("LEDs");
    private final LEDString.Segment ledStringSegment = ledString.makeSegment(0, 7);
    
    public CapstoneLights() {
        
        final Animation rainbowAnim = new RainbowAnimation();
        final JoystickReader buttonCone = RobotProvider.instance.getJoystick(1);
        final JoystickReader buttonCube = RobotProvider.instance.getJoystick(2);
        final JoystickReader buttonDef = RobotProvider.instance.getJoystick(3);

        addRule("Yellow Lights for Cone",
                buttonCone.whenButton(1),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {ledStringSegment.setColor(ColorConstants.YELLOW);});

        addRule("Purple Lights for Cube",
                buttonCube.whenButton(2),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {ledStringSegment.setColor(ColorConstants.PURPLE);});

        addRule("Rainbow Lights for Defense",
                buttonDef.whenButton(3),
                ONCE_AND_HOLD,
                Set.of(ledString),
                () -> {ledStringSegment.animate(rainbowAnim);});
    }
}
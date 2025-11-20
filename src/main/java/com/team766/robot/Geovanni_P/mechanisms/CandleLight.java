package com.team766.robot.Geovanni_P.Mechanisms;


import com.team766.framework.RuleEngine;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader; 
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.reva_2025.constants.InputConstants;
import static com.team766.framework.RulePersistence.*;
import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.RainbowAnimation;



public class CandleLight extends RuleEngine{
    private final LEDString LedString = new LEDString("leds");
    private final LEDString.Segment segment = LEDString.makeSegment(0, 512);

    public static record RobotStatus() implements Status {
        public boolean isReady() {
            return true;
        }
    }
        public CandleLight(JoystickReader JoyStick) {

            addRule(
                "Cone",
                JoyStick.whenButton(InputConstants.GAMEPAD_A_BUTTON),
                ONCE_AND_HOLD,
                segment,
                () -> {
                segment.setColor(ColorConstants.YELLOW);
            });
        
            addRule(
                "Cube",
                JoyStick.whenButton(InputConstants.GAMEPAD_B_BUTTON);
                ONCE_AND_HOLD,
                segment,
                () -> {
                    segment.setColor(ColorConstants.PURPLE);
                });
        
                addRule(
                    "Defense",
                    JoyStick.whenButton(InputConstants.GAMEPAD_Y_BUTTON),
                    ONCE_AND_HOLD,
                    segment,
                    () -> {
                        Animation rainbowAnim = new RainbowAnimation();
                        segment.animate(rainbowAnim);
                    });

                
            }
        }
    
    

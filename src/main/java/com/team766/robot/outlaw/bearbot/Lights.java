package com.team766.robot.outlaw.bearbot;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.FireAnimation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework.RuleEngine;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.outlaw.bearbot.constants.InputConstants;
import edu.wpi.first.wpilibj.DriverStation;

public class Lights extends RuleEngine {
    private static final int LED_COUNT = 90;
    private static final Animation rainbowAnimation = new RainbowAnimation(1, 1.5, LED_COUNT);
    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, LED_COUNT);

    public static record RobotStatus() implements Status {
        public boolean isReady() {
            return true;
        }
    }

    public Lights() {
        final JoystickReader driverController =
                RobotProvider.instance.getJoystick(InputConstants.DRIVER_CONTROLLER);

        addRule(
                "Endgame",
                () ->
                        (DriverStation.isTeleopEnabled()
                                && DriverStation.getMatchTime() < 20), // endgame time
                segment,
                () -> segment.animate(rainbowAnimation));

        addRule(
                "Shooting",
                driverController.whenButton(InputConstants.BUTTON_SHOOT),
                segment,
                () -> {
                    Animation fireAnim = new FireAnimation();
                    segment.animate(fireAnim);
                });
    }

    @Override
    public Category getLoggerCategory() {
        return Category.LIGHTS;
    }
}

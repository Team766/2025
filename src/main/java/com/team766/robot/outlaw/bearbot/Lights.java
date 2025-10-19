package com.team766.robot.outlaw.bearbot;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.FireAnimation;
import com.team766.framework.RuleEngine;
import com.team766.framework.Status;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.LEDString;
import edu.wpi.first.wpilibj.DriverStation;

public class Lights extends RuleEngine {
    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, 512);

    public static record RobotStatus() implements Status {
        public boolean isReady() {
            return true;
        }
    }

    public Lights() {

        addRule(
                "Lights for End Game",
                () ->
                        (DriverStation.isTeleopEnabled()
                                && DriverStation.getMatchTime() < 20), // endgame time
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

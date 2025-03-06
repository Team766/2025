package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.ONCE;

import com.team766.framework3.RuleEngine;
import com.team766.framework3.Status;
import com.team766.framework3.StatusesMixin;
import com.team766.hal.JoystickReader;
import com.team766.logging.Category;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.LEDString;

public class Lights extends RuleEngine implements StatusesMixin {
    // private LEDString leds = new LEDString("leds");
    private final LEDString leds = new LEDString("leds");

    public static record RobotStatus() implements Status {
        public boolean isReady() {
            return true;
        }
    }

    public Lights(JoystickReader boxopGamepad) {

        /* addRule(
                "Algae in intake",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> s.AlgaeInIntake()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.PURPLE);
                });
        addRule(
                "Coral in intake",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> s.CoralInIntake()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.BLUE);
                });
        addRule(
                "Successful Alignment",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> s.Alignment()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.GREEN);
                });
        addRule(
                "End Game",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> s.endGame()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.CYAN);
                }
         addRule(
                "Gyro is 0",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> s.Gyro()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.MAGENTA);
                });
        */

        addRule(
                "Turn on lights",
                boxopGamepad.whenButton(InputConstants.GAMEPAD_START_BUTTON),
                ONCE,
                leds,
                () -> {
                    publishStatus(new RobotStatus());
                });

        addRule(
                "Lights for Testing",
                whenRecentStatusMatching(RobotStatus.class, 2.0, s -> s.isReady()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.PURPLE);
                });
    }

    @Override
    public Category getLoggerCategory() {
        return Category.LIGHTS;
    }
}

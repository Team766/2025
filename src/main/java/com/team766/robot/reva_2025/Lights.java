package com.team766.robot.reva_2025;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.FireAnimation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework3.RuleEngine;
import com.team766.framework3.Status;
import com.team766.framework3.StatusesMixin;
import com.team766.hal.JoystickReader;
import com.team766.logging.Category;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;

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
                "Lights2 for Algae Intake Power",
                whenRecentStatusMatching(
                        AlgaeIntake.AlgaeIntakeStatus.class,
                        2.0,
                        s -> s.state() == AlgaeIntake.State.In),
                leds,
                () -> {
                    leds.setColor(ColorConstants.PURPLE);
                });

        addRule(
                "Lights for Coral Intake",
                whenRecentStatusMatching(
                        CoralIntake.CoralIntakeStatus.class,
                        2.0,
                        s -> s.state() == CoralIntake.State.In),
                leds,
                () -> {
                    leds.setColor(ColorConstants.ORANGE);
                });

        addRule(
                "Lights for Successful Coral Intake",
                whenRecentStatusMatching(
                        CoralIntake.CoralIntakeStatus.class, 2.0, s -> s.coralIntakeSuccessful()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.YELLOW);
                });

        addRule(
                "Lights for Successful Algae Intake",
                whenRecentStatusMatching(
                        AlgaeIntake.AlgaeIntakeStatus.class, 2.0, s -> s.algaeIntakeSuccessful()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.GREEN);
                });

        addRule(
                "Lights for Gyro = 0",
                whenRecentStatusMatching(
                        Climber.ClimberStatus.class, 2.0, s -> s.state() == Climber.State.On),
                leds,
                () -> {
                    Animation rainbowAnim = new RainbowAnimation();
                    leds.animate(rainbowAnim);
                });

        // Doesn't work yet
        addRule(
                "Lights for End Game",
                () -> false,
                leds,
                () -> {
                    Animation fireAnim = new FireAnimation();
                    leds.animate(fireAnim);
                });
    }

    @Override
    public Category getLoggerCategory() {
        return Category.LIGHTS;
    }
}

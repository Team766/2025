package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.ONCE_AND_HOLD;
import static com.team766.framework3.RulePersistence.REPEATEDLY;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.FireAnimation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework3.RuleEngine;
import com.team766.framework3.Status;
import com.team766.framework3.StatusesMixin;
import com.team766.framework3.Conditions.LogicalAnd;
import com.team766.logging.Category;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import edu.wpi.first.wpilibj.DriverStation;

public class Lights extends RuleEngine {
    // private LEDString leds = new LEDString("leds");
    private final LEDString leds = new LEDString("leds");

    public static record RobotStatus() implements Status {
        public boolean isReady() {
            return true;
        }
    }

    public Lights() {

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
                        AlgaeIntake.AlgaeIntakeStatus.class, 2.0, s -> s.isAlgaeStable()),
                leds,
                () -> {
                    leds.setColor(ColorConstants.GREEN);
                });

        addRule(
                "Lights for Algae Intake Power",
                whenStatusMatching(
                        AlgaeIntake.AlgaeIntakeStatus.class,
                        s -> s.state() == AlgaeIntake.State.In),
                leds,
                () -> {
                    leds.setColor(ColorConstants.PURPLE);
                });

        addRule(
                "Lights for Coral Intake",
                whenStatusMatching(
                        CoralIntake.CoralIntakeStatus.class,
                        s -> s.state() == CoralIntake.State.In),
                leds,
                () -> {
                    leds.setColor(ColorConstants.ORANGE);
                });

        addRule(
                "Lights for Gyro = 0",
                new LogicalAnd(whenStatusMatching(SwerveDrive.DriveStatus.class, s -> s.isBalanced()), whenRecentStatusMatching(Climber.ClimberStatus.class, 10.0, s -> s.state() == Climber.State.On)),
                leds,
                () -> {
                    Animation rainbowAnim = new RainbowAnimation();
                    leds.animate(rainbowAnim);
                });

        addRule(
                "Lights for Climb",
                whenRecentStatusMatching(
                        Climber.ClimberStatus.class, 2.0, s -> s.state() == Climber.State.On),
                leds,
                () -> {
                    leds.setColor(ColorConstants.PINK);
                });

        addRule(
                "Lights for End Game",
                () ->
                        (DriverStation.isTeleopEnabled()
                                && DriverStation.getMatchTime() < 20), // endgame time
                leds,
                () -> {
                    Animation fireAnim = new FireAnimation();
                    leds.animate(fireAnim);
                });

        addRule(
                "Default",
                () -> true,
                leds,
                () -> {
                leds.animate(null);
                leds.setColor(0,0,0);
                }
        );
    }

    @Override
    public Category getLoggerCategory() {
        return Category.LIGHTS;
    }
}

package com.team766.robot.reva_2025;

import com.team766.framework.Conditions;
import com.team766.framework.Conditions.LogicalAnd;
import com.team766.framework.RuleEngine;
import com.team766.framework.Status;
import com.team766.logging.Category;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
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
                "Lights for Successful Coral Intake",
                new Conditions.TimedLatch(
                        whenStatusMatching(
                                CoralIntake.CoralIntakeStatus.class,
                                s -> s.coralIntakeSuccessful()),
                        2),
                segment,
                () -> {
                    segment.setColor(ColorConstants.YELLOW);
                });

        addRule(
                "Lights for Successful Algae Intake",
                new Conditions.TimedLatch(
                        whenStatusMatching(
                                AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAlgaeStable()),
                        2),
                segment,
                () -> {
                    segment.setColor(ColorConstants.GREEN);
                });

        addRule(
                "Lights for Algae Intake Power",
                whenStatusMatching(
                        AlgaeIntake.AlgaeIntakeStatus.class,
                        s -> s.state() == AlgaeIntake.State.In),
                segment,
                () -> {
                    segment.setColor(ColorConstants.PURPLE);
                });

        addRule(
                "Lights for Coral Intake",
                whenStatusMatching(
                        CoralIntake.CoralIntakeStatus.class,
                        s -> s.state() == CoralIntake.State.In),
                segment,
                () -> {
                    segment.setColor(ColorConstants.ORANGE);
                });

        addRule(
                "Lights for Gyro = 0",
                new LogicalAnd(
                        whenStatusMatching(SwerveDrive.DriveStatus.class, s -> s.isBalanced()),
                        new Conditions.TimedLatch(
                                whenStatusMatching(
                                        Climber.ClimberStatus.class,
                                        s -> s.state() == Climber.State.On),
                                5),
                        new Conditions.TimedLatch(
                                whenStatusMatching(
                                        SwerveDrive.DriveStatus.class, s -> !s.isBalanced()),
                                20)),
                segment,
                () -> {
                    Animation rainbowAnim = new RainbowAnimation();
                    segment.animate(rainbowAnim);
                });

        addRule(
                "Lights for Climb",
                whenStatusMatching(Climber.ClimberStatus.class, s -> s.state() == Climber.State.On),
                segment,
                () -> {
                    segment.setColor(ColorConstants.PINK);
                });

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

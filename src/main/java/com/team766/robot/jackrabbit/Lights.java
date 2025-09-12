package com.team766.robot.jackrabbit;

import com.ctre.phoenix.led.StrobeAnimation;
import com.team766.framework.RuleEngine;
import com.team766.framework.RuleGroup;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.jackrabbit.mechanisms.Drive;
import com.team766.robot.jackrabbit.mechanisms.Hood;
import com.team766.robot.jackrabbit.mechanisms.Shooter;
import com.team766.robot.jackrabbit.mechanisms.Turret;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

public class Lights extends RuleEngine {
    private static final int LED_COUNT = 90;
    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, LED_COUNT);

    public Lights() {
        addRule(
                "Turret uninitialized",
                whenStatusMatching(Turret.TurretStatus.class, s -> !s.initialized()),
                segment,
                () -> segment.setColor(Color.kRed));

        addRule("When disabled", () -> DriverStation.isDisabled())
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Not localized",
                                        whenStatusMatching(
                                                Drive.DriveStatus.class,
                                                s -> !s.currentPosition().isPresent()),
                                        segment,
                                        () -> segment.setColor(Color.kYellow));

                                addRule(
                                        "Ready to begin match",
                                        UNCONDITIONAL,
                                        segment,
                                        () -> segment.setColor(Color.kGreen));
                            }
                        });

        addRule(
                "Selected shot",
                whenStatus(OI.OIStatus.class),
                segment,
                () -> {
                    final var color =
                            switch (getStatusOrThrow(OI.OIStatus.class).shotTarget()) {
                                case SHORT -> Color.kTeal;
                                case KRAKEN -> Color.kOrange;
                                case AUTO -> Color.kGreen;
                            };
                    final var color8 = new Color8Bit(color);
                    final boolean atTarget =
                            checkForStatusMatching(Turret.TurretStatus.class, s -> s.isAtTarget())
                                    && checkForStatusMatching(
                                            Hood.HoodStatus.class, s -> s.isAtTarget())
                                    && checkForStatusMatching(
                                            Shooter.ShooterStatus.class, s -> s.isAtTarget());
                    if (atTarget) {
                        segment.animate(new StrobeAnimation(color8.red, color8.green, color8.blue));
                    } else {
                        segment.setColor(color);
                    }
                });
    }
}

package com.team766.robot.gatorade;

import static com.team766.framework.RulePersistence.*;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework.RuleEngine;
import com.team766.logging.Severity;
import com.team766.robot.common.mechanisms.LEDString;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;

public class Lights extends RuleEngine {
    private static final int LED_COUNT = 90;
    private static final Animation rainbowAnimation = new RainbowAnimation(1, 1.5, LED_COUNT);
    private final LEDString leds = new LEDString("leds");

    public Lights() {
        addRule(
                "OI State Updated",
                () -> checkForRecentStatus(OI.OIStatus.class, 1.3),
                REPEATEDLY,
                leds,
                () -> {
                    final OI.OIStatus status = getStatusOrThrow(OI.OIStatus.class);
                    setLightsForGamePiece(status.gamePieceType());
                    // setLightsForPlacement(status.placementPosition(), status.gamePieceType());
                });

        addRule(
                "Endgame",
                () -> DriverStation.getMatchTime() > 0 && DriverStation.getMatchTime() < 17,
                ONCE_AND_HOLD,
                leds,
                () -> leds.animate(rainbowAnimation));

        addRule(
                "Default display",
                () -> checkForStatus(OI.OIStatus.class),
                REPEATEDLY,
                leds,
                () -> {
                    final OI.OIStatus status = getStatusOrThrow(OI.OIStatus.class);
                    setLightsForGamePiece(status.gamePieceType());
                    // setLightsForPlacement(status.placementPosition(), status.gamePieceType());
                });
    }

    private void setLightsForPlacement(
            PlacementPosition placementPosition, GamePieceType gamePieceType) {
        switch (placementPosition) {
            case NONE -> leds.setColor(Color.kWhite);
            case LOW_NODE -> leds.setColor(Color.kLime);
            case MID_NODE -> leds.setColor(Color.kRed);
            case HIGH_NODE -> leds.setColor(Color.kOrangeRed);
            case HUMAN_PLAYER -> setLightsForGamePiece(gamePieceType);
            default ->
            // warn, ignore
            log(Severity.WARNING, "Unexpected placement position: " + placementPosition.toString());
        }
    }

    private void setLightsForGamePiece(GamePieceType gamePieceType) {
        switch (gamePieceType) {
            case CUBE -> leds.setColor(Color.kPurple);
            case CONE -> leds.setColor(Color.kOrange);
        }
    }
}

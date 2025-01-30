package com.team766.robot.reva;

import com.team766.framework3.Context;
import com.team766.framework3.RuleEngine;
import com.team766.framework3.RuleGroup;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.reva.mechanisms.ForwardApriltagCamera;
import com.team766.robot.reva.mechanisms.Orin;
import com.team766.robot.reva.procedures.IntakeUntilIn;
import com.team766.robot.reva.procedures.ShootingProcedureStatus;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.BooleanSupplier;

public class Lights extends RuleEngine {

    private final LEDString leds = new LEDString("leds");

    public Lights() {
        final BooleanSupplier isCameraMissing =
                () ->
                        !checkForStatusMatching(
                                ForwardApriltagCamera.ApriltagCameraStatus.class,
                                s -> s.isCameraConnected());

        addRule("Robot Disabled", () -> DriverStation.isDisabled())
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Camera Missing",
                                        isCameraMissing,
                                        leds,
                                        () -> signalCameraNotConnected());

                                addRule(
                                        "Alliance Color",
                                        UNCONDITIONAL,
                                        leds,
                                        () -> {
                                            var alliance = DriverStation.getAlliance();
                                            if (alliance.isEmpty()) {
                                                purple();
                                                return;
                                            }
                                            switch (alliance.orElseThrow()) {
                                                case Blue -> blue();
                                                case Red -> red();
                                            }
                                        });
                            }
                        });

        addRule(
                "Note in intake",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> s.noteInIntake()),
                leds,
                () -> signalNoteInIntake());
        addRule(
                "No note in intake yet",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> !s.noteInIntake()),
                leds,
                () -> signalNoNoteInIntakeYet());

        addRule(
                "Shooting procedure: camera disconnected",
                () ->
                        checkForRecentStatus(ShootingProcedureStatus.class, 2.0)
                                && isCameraMissing.getAsBoolean(),
                leds,
                () -> signalCameraNotConnected());
        addRule(
                "Shooting procedure: starting",
                whenRecentStatusMatching(
                        ShootingProcedureStatus.class,
                        2.0,
                        s -> s.status() == ShootingProcedureStatus.Status.RUNNING),
                leds,
                () -> signalStartingShootingProcedure());
        addRule(
                "Shooting procedure: out of range",
                whenRecentStatusMatching(
                        ShootingProcedureStatus.class,
                        2.0,
                        s -> s.status() == ShootingProcedureStatus.Status.OUT_OF_RANGE),
                leds,
                context -> signalShooterOutOfRange(context));
        addRule(
                "Shooting procedure: finished",
                whenRecentStatusMatching(
                        ShootingProcedureStatus.class,
                        2.0,
                        s -> s.status() == ShootingProcedureStatus.Status.FINISHED),
                leds,
                () -> signalFinishingShootingProcedure());

        addRule(
                "Has tag",
                whenStatusMatching(Orin.OrinStatus.class, s -> s.apriltags().size() > 0),
                leds,
                () -> signalHasTag());

        addRule("No display", UNCONDITIONAL, leds, () -> turnLightsOff());
    }

    // Lime green
    public void signalCameraConnected() {
        leds.setColor(92, 250, 40);
    }

    public void signalFinishedShootingProcedure() {
        leds.setColor(0, 150, 0);
    }

    // Purple
    public void signalCameraNotConnected() {
        leds.setColor(100, 0, 100);
    }

    public void signalShooterOutOfRange(Context context) {
        while (true) {
            leds.setColor(150, 0, 0);
            context.waitForSeconds(0.5);
            turnLightsOff();
            context.waitForSeconds(0.5);
        }
    }

    // Coral orange
    public void signalNoteInIntake() {
        leds.setColor(255, 95, 21);
    }

    // Off
    public void turnLightsOff() {
        leds.setColor(0, 0, 0);
    }

    // Blue
    public void signalNoNoteInIntakeYet() {
        leds.setColor(0, 0, 100);
    }

    public void isDoingShootingProcedure() {
        leds.setColor(0, 227, 197);
    }

    public void signalFinishingShootingProcedure() {
        leds.setColor(0, 50, 100);
    }

    public void signalStartingShootingProcedure() {
        leds.setColor(50, 50, 2);
    }

    public void signalHasTag() {
        leds.setColor(255, 215, 0);
    }

    public void red() {
        leds.setColor(100, 0, 0);
    }

    public void blue() {
        leds.setColor(0, 0, 100);
    }

    public void purple() {
        leds.setColor(100, 0, 100);
    }
}

package com.team766.robot.reva;

import com.team766.framework.Context;
import com.team766.framework.RuleEngine;
import com.team766.framework.RuleGroup;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.reva.mechanisms.ForwardApriltagCamera;
import com.team766.robot.reva.mechanisms.Orin;
import com.team766.robot.reva.procedures.IntakeUntilIn;
import com.team766.robot.reva.procedures.ShootingProcedureStatus;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.BooleanSupplier;

public class Lights extends RuleEngine {

    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, 512);

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
                                        segment,
                                        () -> signalCameraNotConnected());

                                addRule(
                                        "Alliance Color",
                                        UNCONDITIONAL,
                                        segment,
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
                segment,
                () -> signalNoteInIntake());
        addRule(
                "No note in intake yet",
                whenRecentStatusMatching(
                        IntakeUntilIn.IntakeUntilInStatus.class, 2.0, s -> !s.noteInIntake()),
                segment,
                () -> signalNoNoteInIntakeYet());

        addRule(
                "Shooting procedure: camera disconnected",
                () ->
                        checkForRecentStatus(ShootingProcedureStatus.class, 2.0)
                                && isCameraMissing.getAsBoolean(),
                segment,
                () -> signalCameraNotConnected());
        addRule(
                "Shooting procedure: starting",
                whenRecentStatusMatching(
                        ShootingProcedureStatus.class,
                        2.0,
                        s -> s.status() == ShootingProcedureStatus.Status.RUNNING),
                segment,
                () -> signalStartingShootingProcedure());
        addRule(
                "Shooting procedure: out of range",
                whenRecentStatusMatching(
                        ShootingProcedureStatus.class,
                        2.0,
                        s -> s.status() == ShootingProcedureStatus.Status.OUT_OF_RANGE),
                segment,
                context -> signalShooterOutOfRange(context));
        addRule(
                "Shooting procedure: finished",
                whenRecentStatusMatching(
                        ShootingProcedureStatus.class,
                        2.0,
                        s -> s.status() == ShootingProcedureStatus.Status.FINISHED),
                segment,
                () -> signalFinishingShootingProcedure());

        addRule(
                "Has tag",
                whenStatusMatching(Orin.OrinStatus.class, s -> s.apriltags().size() > 0),
                segment,
                () -> signalHasTag());

        addRule("No display", UNCONDITIONAL, segment, () -> turnLightsOff());
    }

    // Lime green
    public void signalCameraConnected() {
        segment.setColor(92, 250, 40);
    }

    public void signalFinishedShootingProcedure() {
        segment.setColor(0, 150, 0);
    }

    // Purple
    public void signalCameraNotConnected() {
        segment.setColor(100, 0, 100);
    }

    public void signalShooterOutOfRange(Context context) {
        while (true) {
            segment.setColor(150, 0, 0);
            context.waitForSeconds(0.5);
            turnLightsOff();
            context.waitForSeconds(0.5);
        }
    }

    // Coral orange
    public void signalNoteInIntake() {
        segment.setColor(255, 95, 21);
    }

    // Off
    public void turnLightsOff() {
        segment.setColor(0, 0, 0);
    }

    // Blue
    public void signalNoNoteInIntakeYet() {
        segment.setColor(0, 0, 100);
    }

    public void isDoingShootingProcedure() {
        segment.setColor(0, 227, 197);
    }

    public void signalFinishingShootingProcedure() {
        segment.setColor(0, 50, 100);
    }

    public void signalStartingShootingProcedure() {
        segment.setColor(50, 50, 2);
    }

    public void signalHasTag() {
        segment.setColor(255, 215, 0);
    }

    public void red() {
        segment.setColor(100, 0, 0);
    }

    public void blue() {
        segment.setColor(0, 0, 100);
    }

    public void purple() {
        segment.setColor(100, 0, 100);
    }
}

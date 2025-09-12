package com.team766.robot.jackrabbit;

import com.team766.framework.RuleEngine;
import com.team766.framework.RulePersistence;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.jackrabbit.mechanisms.*;
import com.team766.robot.jackrabbit.procedures.*;
import java.util.Set;

public class OI extends RuleEngine {
    public enum ShotTarget {
        SHORT,
        KRAKEN,
        AUTO,
    }

    public record OIStatus(ShotTarget shotTarget) implements Status {}

    private static final int AXIS_DRIVE_X = InputConstants.GAMEPAD_LEFT_STICK_XAXIS;
    private static final int AXIS_DRIVE_Y = InputConstants.GAMEPAD_LEFT_STICK_YAXIS;
    private static final int AXIS_TURRET = InputConstants.GAMEPAD_RIGHT_STICK_XAXIS;
    private static final int AXIS_HOOD = InputConstants.GAMEPAD_RIGHT_STICK_YAXIS;

    private ShotTarget shotTarget = ShotTarget.SHORT;

    public OI(
            Drive drive,
            Collector collector,
            Spindexer spindexer,
            Feeder feeder,
            Turret turret,
            Hood hood,
            Shooter shooter) {
        final JoystickReader gamepad = RobotProvider.instance.getJoystick(0);

        gamepad.setAxisDeadzone(AXIS_DRIVE_X, 0.05);
        gamepad.setAxisDeadzone(AXIS_DRIVE_Y, 0.05);
        gamepad.setAxisDeadzone(AXIS_TURRET, 0.5);
        gamepad.setAxisDeadzone(AXIS_HOOD, 0.5);
        gamepad.setAxisDeadzone(InputConstants.GAMEPAD_LEFT_TRIGGER, 0.5);
        gamepad.setAxisDeadzone(InputConstants.GAMEPAD_RIGHT_TRIGGER, 0.5);

        addRule(
                "Reset turret",
                gamepad.whenButton(InputConstants.GAMEPAD_BACK_BUTTON),
                () -> new InitializeTurret(turret));

        addRule(
                "Manual drive",
                gamepad.whenAnyAxisMoved(AXIS_DRIVE_X, AXIS_DRIVE_Y),
                RulePersistence.REPEATEDLY,
                drive,
                () ->
                        drive.driveFieldOriented(
                                -gamepad.getAxis(AXIS_DRIVE_Y), -gamepad.getAxis(AXIS_DRIVE_X)));

        addRule(
                "Center turret",
                gamepad.whenButton(InputConstants.GAMEPAD_B_BUTTON),
                turret,
                () -> turret.setTargetAngle(0.0));

        addRule(
                "Select Short Shot",
                gamepad.whenButton(InputConstants.GAMEPAD_A_BUTTON),
                Set.of(),
                () -> shotTarget = ShotTarget.SHORT);

        addRule(
                "Select Kraken Shot",
                gamepad.whenButton(InputConstants.GAMEPAD_X_BUTTON),
                Set.of(),
                () -> shotTarget = ShotTarget.KRAKEN);

        addRule(
                "Select Auto aim",
                gamepad.whenButton(InputConstants.GAMEPAD_Y_BUTTON),
                Set.of(),
                () -> shotTarget = ShotTarget.AUTO);

        addRule(
                "Apply Shot",
                gamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER),
                RulePersistence.REPEATEDLY,
                () ->
                        switch (shotTarget) {
                            case SHORT -> new ShortShot(hood, shooter);
                            case KRAKEN -> new KrakenShot(hood, shooter);
                            case AUTO -> new AutoShot(turret, hood, shooter);
                        });

        addRule(
                        "Manual turret",
                        gamepad.whenAxisMoved(AXIS_TURRET),
                        RulePersistence.REPEATEDLY,
                        turret,
                        () -> turret.move(-gamepad.getAxis(AXIS_TURRET) / 3.0))
                .withFinishedTriggeringProcedure(turret, () -> turret.stop());

        addRule(
                        "Manual hood",
                        gamepad.whenAxisMoved(AXIS_HOOD),
                        RulePersistence.REPEATEDLY,
                        hood,
                        () -> hood.move(gamepad.getAxis(AXIS_HOOD) / 3.0))
                .withFinishedTriggeringProcedure(hood, () -> hood.stop());

        addRule(
                "Feed shooter",
                gamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                () -> new Shoot(spindexer, feeder));

        addRule(
                "Unshoot",
                gamepad.whenButton(InputConstants.GAMEPAD_START_BUTTON),
                () -> new Unshoot(spindexer, feeder, shooter));

        addRule(
                "Intake",
                gamepad.whenAxisMoved(InputConstants.GAMEPAD_LEFT_TRIGGER),
                () -> new Intake(collector, spindexer, feeder));

        addRule(
                "Outtake",
                gamepad.whenButton(InputConstants.GAMEPAD_LEFT_BUMPER_BUTTON),
                () -> new Outtake(collector));
    }

    @Override
    protected void updateStatus() {
        publishStatus(new OIStatus(shotTarget));
    }
}

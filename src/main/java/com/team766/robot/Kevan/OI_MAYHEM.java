package com.team766.robot.Kevan;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.Kevan.mechanisms.Intake;
import com.team766.robot.Kevan.mechanisms.Shooter;
import com.team766.robot.common.constants.InputConstants;
import java.util.Set;

public class OI_MAYHEM extends RuleEngine {
    public OI_MAYHEM(Drive drive, Shooter shooter, Intake intake) {
        final JoystickReader gamePad1 = RobotProvider.instance.getJoystick(0);
        addRule(
                "RUN_DRIVE",
                gamePad1.whenAnyAxisMoved(1, 5),
                REPEATEDLY,
                Set.of(drive),
                () -> {
                    drive.move_left(gamePad1.getAxis(1));
                    drive.move_right(gamePad1.getAxis(5));
                });
        addRule(
                        "RUN_INTAKE",
                        gamePad1.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                        ONCE_AND_HOLD,
                        Set.of(intake),
                        () -> {
                            intake.SetIntake(1);
                        })
                .withFinishedTriggeringProcedure(
                        intake,
                        () -> {
                            intake.SetIntake(0);
                        });
        addRule(
                        "SHOOT_SET_POWER",
                        gamePad1.whenAnyAxisMoved(2, 3),
                        REPEATEDLY,
                        Set.of(shooter),
                        (context) -> {
                            shooter.SetShooterSpeed(gamePad1.getAxis(3));
                            shooter.SetTransferSpeed(gamePad1.getAxis(2));
                        })
                .withFinishedTriggeringProcedure(
                        shooter,
                        () -> {
                            shooter.SetTransferSpeed(0);
                            shooter.SetShooterSpeed(0);
                        });
    }
}

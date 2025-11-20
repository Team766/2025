package com.team766.robot.StephenCheung;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.StephenCheung.mechanisms.Drive;
import com.team766.robot.StephenCheung.mechanisms.Intake;
import com.team766.robot.StephenCheung.mechanisms.Shooter;
import com.team766.robot.common.constants.InputConstants;
import java.util.Set;

public class OI extends RuleEngine {
    public OI(Drive drive, Shooter shooter, Intake intake) {
        final JoystickReader gamePad = RobotProvider.instance.getJoystick(0);
        addRule(
                "RUN_LEFT_MOTOR",
                gamePad.whenAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_YAXIS),
                REPEATEDLY,
                Set.of(drive),
                () -> {
                    drive.move_left(gamePad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS));
                });
        addRule(
                "RUN_RIGHT_MOTOR",
                gamePad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS),
                REPEATEDLY,
                Set.of(drive),
                () -> {
                    drive.move_right(gamePad.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS));
                });

        addRule(
                        "RUN_INTAKE",
                        gamePad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
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
                        gamePad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER),
                        REPEATEDLY,
                        Set.of(shooter),
                        (context) -> {
                            shooter.SetShooterSpeed(
                                    gamePad.getAxis(InputConstants.GAMEPAD_RIGHT_TRIGGER));
                            context.waitForSeconds(0.25);
                            shooter.SetTransferSpeed(1);
                        })
                .withFinishedTriggeringProcedure(
                        shooter,
                        () -> {
                            shooter.SetTransferSpeed(0);
                        });
    }
}

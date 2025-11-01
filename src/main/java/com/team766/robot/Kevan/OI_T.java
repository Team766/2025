package com.team766.robot.Kevan;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.common.constants.InputConstants;
import java.util.Set;
import com.team766.robot.Kevan.mechanisms.Intake;
import com.team766.robot.Kevan.mechanisms.Shooter;

public class OI_T extends RuleGroup {
    public OI_T(Drive drive, Shooter shooter, Intake intake) {
        final JoystickReader gamePad1 = RobotProvider.instance.getJoystick(0);
        addRule(
                "RUN_LEFT_MOTOR",
                gamePad1.whenAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_YAXIS),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_left(gamePad1.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS));
                });

        addRule(
                "RUN_RIGHT_MOTOR",
                gamePad1.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_right(gamePad1.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS));
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
                gamePad1.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER),
                ONCE_AND_HOLD,
                Set.of(shooter),
                () -> {
                    shooter.SetShooterSpeed(gamePad1.getAxis(InputConstants.GAMEPAD_RIGHT_TRIGGER));
                })
                .withFinishedTriggeringProcedure(
                    shooter,
                    () -> {
                        shooter.SetShooterSpeed(0);
                });
    }
}

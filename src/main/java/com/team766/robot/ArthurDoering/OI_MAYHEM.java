package com.team766.robot.ArthurDoering;

import static com.team766.framework.RulePersistence.*;
import com.team766.framework.Context;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Intake;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;
import com.team766.robot.common.constants.InputConstants;
import java.util.Set;

public class OI_MAYHEM extends RuleEngine {
    public OI_MAYHEM(Drive drive, Intake intake, Shooter shoot, Context context) {
        final JoystickReader gamepad = RobotProvider.instance.getJoystick(0);
        addRule(
                "RUN_LEFT_MOTOR",
                gamepad.whenAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_YAXIS),
                REPEATEDLY,
                Set.of(drive),
                () -> {
                    drive.move_left(gamepad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS));
                });

        addRule(
                "RUN_RIGHT_MOTOR",
                gamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS),
                REPEATEDLY,
                Set.of(drive),
                () -> {
                    drive.move_right(gamepad.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS));
                });
        addRule(
                        "RUN_INTAKE",
                        gamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                        ONCE_AND_HOLD,
                        Set.of(intake),
                        () -> {
                            intake.setIntake(1);
                        })
                .withFinishedTriggeringProcedure(
                        intake,
                        () -> {
                            intake.setIntake(0);
                        });
        addRule(
                        "SHOOT_SET_POWER",
                        gamepad.whenButton(InputConstants.GAMEPAD_RIGHT_TRIGGER),
                        ONCE_AND_HOLD,
                        Set.of(shoot),
                        () -> {
                            shoot.SetShooterSpeed(1);
                            context.waitForSeconds(1);
                            shoot.SetTransferSpeed(1);
                        })
                .withFinishedTriggeringProcedure(
                        intake,
                        () -> {
                            shoot.SetTransferSpeed(0);
                            shoot.SetShooterSpeed(0);
                        });
    }
}

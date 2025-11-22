package com.team766.robot.ArthurDoering;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Intake;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;
import com.team766.robot.common.constants.InputConstants;
import java.util.Set;

public class OI_MAYHEM extends RuleEngine {
    public OI_MAYHEM(Drive drive, Intake intake, Shooter shoot) {
        final JoystickReader gamepad = RobotProvider.instance.getJoystick(0);
        addRule(
                "RUN DRIVE",
                gamepad.whenAnyAxisMoved(1, 5),
                REPEATEDLY,
                Set.of(drive),
                () -> {
                    drive.move_left(gamepad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS));
                    drive.move_right(gamepad.getAxis(5));
                });
        addRule(
                        "RUN_INTAKE",
                        gamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                        ONCE_AND_HOLD,
                        Set.of(intake),
                        () -> {
                            intake.setIntake(0.5);
                        })
                .withFinishedTriggeringProcedure(
                        intake,
                        () -> {
                            intake.setIntake(0);
                        });
        addRule(
                        "SHOOT_SET_POWER",
                        gamepad.whenAnyAxisMoved(2,3),
                        REPEATEDLY,
                        Set.of(shoot),
                        () -> {
                            shoot.SetShooterSpeed(gamepad.getAxis(3));
                            shoot.SetTransferSpeed(gamepad.getAxis(2));
                        })
                .withFinishedTriggeringProcedure(
                        intake,
                        () -> {
                            shoot.SetTransferSpeed(0);
                        });
    }
}

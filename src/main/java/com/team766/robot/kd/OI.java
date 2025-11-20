package com.team766.robot.kd;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.kd.mechanisms.*;
import java.util.Set;

public class OI extends RuleEngine {
    public OI(Drive drive, Shooter shooter, Loader loader, Intake intake) {
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(0);
        addRule(
                "handle_axis_moved",
                joystick1.whenAnyAxisMoved(
                        InputConstants.AXIS_FORWARD_BACKWARD, InputConstants.AXIS_LEFT_RIGHT),
                REPEATEDLY,
                Set.of(drive),
                () -> {
                    double forward_backward =
                            joystick1.getAxis(InputConstants.AXIS_FORWARD_BACKWARD);
                    double left_right = joystick1.getAxis(InputConstants.AXIS_LEFT_RIGHT);
                    drive.move_left(left_right + forward_backward);
                    drive.move_right(forward_backward - left_right);
                });

        addRule(
                "run_shooter",
                joystick1.whenAxisMoved(InputConstants.JOYSTICK_TRIGGER),
                REPEATEDLY,
                Set.of(shooter),
                () -> {
                    shooter.run(joystick1.getAxis(InputConstants.JOYSTICK_TRIGGER));
                });

        addRule(
                        "run_loader",
                        joystick1.whenButton(InputConstants.JOYSTICK_BOTTOM_BUTTON),
                        ONCE_AND_HOLD,
                        Set.of(loader),
                        () -> {
                            loader.run();
                        })
                .withFinishedTriggeringProcedure(
                        loader,
                        () -> {
                            loader.stop();
                        });

        addRule(
                        "run_intake",
                        joystick1.whenButton(InputConstants.JOYSTICK_LEFT_BUTTON),
                        ONCE_AND_HOLD,
                        Set.of(intake),
                        () -> {
                            intake.run();
                        })
                .withFinishedTriggeringProcedure(
                        intake,
                        () -> {
                            intake.stop();
                        });
    }
}

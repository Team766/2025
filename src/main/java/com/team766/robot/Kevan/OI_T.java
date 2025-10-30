package com.team766.robot.Kevan;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.common.constants.InputConstants;
import java.util.Set;

public class OI_T extends RuleGroup {
    public OI_T(Drive drive) {
        final JoystickReader leftJoystick = RobotProvider.instance.getJoystick(0);
        final JoystickReader rightJoystick = RobotProvider.instance.getJoystick(1);

        addRule(
                "RUN_LEFT_MOTOR",
                leftJoystick.whenAnyAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_left(leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));
                });

        addRule(
                "RUN_RIGHT_MOTOR",
                rightJoystick.whenAnyAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_right(rightJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));
                });
    }
}

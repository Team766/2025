package com.team766.robot.mark;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.mark.mechanisms.Drive;
import java.util.Set;

public class OI extends RuleEngine {
    public OI(Drive drive) {
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(0);

        addRule(
                "handle_axis_moved",
                joystick1.whenAnyAxisMoved(
                        InputConstants.AXIS_FORWARD_BACKWARD, InputConstants.AXIS_LEFT_RIGHT),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    double forward_backward =
                            joystick1.getAxis(InputConstants.AXIS_FORWARD_BACKWARD);
                    double left_right = joystick1.getAxis(InputConstants.AXIS_LEFT_RIGHT);
                    drive.move_left(left_right + forward_backward);
                    drive.move_right(forward_backward - left_right);
                });
    }
}

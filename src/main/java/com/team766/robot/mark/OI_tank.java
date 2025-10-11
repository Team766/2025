package com.team766.robot.mark;

import java.util.Set;
import static com.team766.framework.RulePersistence.*;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotProvider;
import com.team766.robot.mark.mechanisms.Drive;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.InputConstants;

public class OI_tank extends RuleEngine {
    public OI_tank(Drive drive) {
        final JoystickReader leftJoystick = RobotProvider.instance.getJoystick(0);
        final JoystickReader rightJoystick = RobotProvider.instance.getJoystick(1);
        addRule("RUN_LEFT_MOTOR",
                leftJoystick.whenAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_left(leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));
                }
        );

        addRule("RUN_RIGHT_MOTOR",
                rightJoystick.whenAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.move_right(rightJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));
                }
        );
    }
}
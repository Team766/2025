package com.team766.robot.ArthurDoering.OI;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.common.constants.InputConstants;
import java.util.Set;

public class OI_T extends RuleGroup {
    public OI_T(Drive drive) {
        final JoystickReader leftJoystick = RobotProvider.instance.getJoystick(0);
        final JoystickReader rightJoystick = RobotProvider.instance.getJoystick(1);
        addRule(
                "Run_Left_MOTOR",
                leftJoystick.whenAnyAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.moveLeft(leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));
                });

        addRule(
                "Run_Right_MOTOR",
                rightJoystick.whenAnyAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
                ONCE_AND_HOLD,
                Set.of(drive),
                () -> {
                    drive.moveRight(rightJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));
                });
    }
}

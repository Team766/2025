package com.team766.robot.ArthurDoering.OI;
import java.util.Set;

import static com.team766.framework.RulePersistence.*;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.common.constants.InputConstants;

public class OI_A extends RuleGroup {
    public OI_A(Drive drive) {
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(0);
        addRule("handle_axis_moved", 
            joystick1.whenAnyAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD, InputConstants.AXIS_LEFT_RIGHT), 
            ONCE_AND_HOLD, 
            Set.of(drive), 
            () -> {
                double forward_backward = joystick1.getAxis(InputConstants.AXIS_FORWARD_BACKWARD);
                double left_right = joystick1.getAxis(InputConstants.AXIS_LEFT_RIGHT);
                drive.move_left(forward_backward + left_right);
                drive.move_right(forward_backward - left_right);
            });
    }
}
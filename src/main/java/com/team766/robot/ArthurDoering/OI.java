package com.team766.robot.ArthurDoering;

import static com.team766.framework.RulePersistence.*;
import java.util.Set;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.ArthurDoering.mechanisms.MovingMotor;

public class OI extends RuleEngine {
    public OI(Drive drive) {
        final JoystickReader leftJoystick = RobotProvider.instance.getJoystick(index: 0);
        final JoystickReader rightJoystick = RobotProvider.instance.getJoystick(index: 1);
        addRule("Run_Left_MOTOR",
        leftJoystick.isAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
        ONCE_AND_HOLD,
        Set.of(drive),
        () -> {drive.move_left(leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));});
        
        addRule("Run_Right_MOTOR",
        rightJoystick.isAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
        ONCE_AND_HOLD,
        Set.of(drive),
        () -> {drive.move_right(rightJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));});
    }
}

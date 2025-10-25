package com.team766.robot.ArthurDoering.mechanisms;

import static com.team766.framework.RulePersistence.*;

import java.util.Set;
import com.team766.framework.RuleEngine;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.ArthurDoering.mechanisms.MovingMotor;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.framework.Conditions;

public class OI extends RuleEngine {
    public OI(Drive drive) {
        final JoystickReader leftJoystick = RobotProvider.instance.getJoystick(0);
        final JoystickReader rightJoystick = RobotProvider.instance.getJoystick(1);
        addRule("Run_Left_MOTOR",
        leftJoystick.isAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
        ONCE_AND_HOLD,
        Set.of(drive),
        () -> {drive.moveLeft(leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));});
        
        addRule("Run_Right_MOTOR",
        rightJoystick.isAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD),
        ONCE_AND_HOLD,
        Set.of(drive),
        () -> {drive.moveRight(rightJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD));});
    }
}

package com.team766.robot.outlaw.bearbot;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.common.DriverOI;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.outlaw.bearbot.constants.InputConstants;

public class OI extends RuleEngine {
    public OI(SwerveDrive drive) {
        final JoystickReader leftJoystick =
                RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
        final JoystickReader rightJoystick =
                RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);

        // Add driver control rules here.
        addRules(new DriverOI(leftJoystick, rightJoystick, drive));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

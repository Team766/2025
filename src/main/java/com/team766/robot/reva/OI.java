package com.team766.robot.reva;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.constants.InputConstants;
import com.team766.robot.reva.mechanisms.*;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    public OI(
            SwerveDrive drive, Climber climber, Shoulder shoulder, Intake intake, Shooter shooter) {
        final JoystickReader leftJoystick =
                RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
        final JoystickReader rightJoystick =
                RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);
        final JoystickReader macropad = RobotProvider.instance.getJoystick(InputConstants.MACROPAD);
        final JoystickReader gamepad =
                RobotProvider.instance.getJoystick(InputConstants.BOXOP_GAMEPAD_X);

        // Add driver control rules here.

        // Driver OI: take input from left, right joysticks.  control drive.
        addRules(new DriverOI(leftJoystick, rightJoystick, drive, shoulder, intake));
        addRules(new BoxOpOI(gamepad, climber, shoulder, intake, shooter));
        // Debug OI: allow for finer-grain testing of each mechanism.
        addRules(new DebugOI(macropad, climber, shoulder, intake, shooter));
    }
}

package com.team766.robot.reva_2025.OI;

import com.team766.framework3.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.common.DriverOI;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.*;

public class OI extends RuleEngine {
    public OI(
            SwerveDrive drive,
            AlgaeIntake algaeIntake,
            Wrist wrist,
            Climber climber,
            Elevator elevator,
            CoralIntake coral) {
        final JoystickReader leftJoystick =
                RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
        final JoystickReader rightJoystick =
                RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);
        final JoystickReader boxopGamepad =
                RobotProvider.instance.getJoystick(InputConstants.BOXOP_GAMEPAD);
        final JoystickReader macropad = RobotProvider.instance.getJoystick(InputConstants.MACROPAD);

        // Add driver control rules here.

        addRules(new DebugOI(macropad, climber, elevator, wrist, algaeIntake, coral));
        addRules(new DriverOI(leftJoystick, rightJoystick, drive));
        // addRules(new AlgaeIntakeOI(boxopGamepad, algaeIntake));
        // addRules(new WristOI(boxopGamepad, wrist));
        // addRules(new ClimberOI(boxopGamepad, climber));s
        // addRules(new ElevatorOI(boxopGamepad, elevator));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

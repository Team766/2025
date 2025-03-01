package com.team766.robot.reva_2025;

import com.team766.framework3.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class OI extends RuleEngine {
    private final boolean useGamePadForDriverControls = true;

    public OI(
            SwerveDrive drive,
            AlgaeIntake algaeIntake,
            Wrist wrist,
            Elevator elevator,
            CoralIntake coralIntake,
            Climber climber) {
        if (useGamePadForDriverControls == false) {
            final JoystickReader leftJoystick =
                    RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
            final JoystickReader rightJoystick =
                    RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);
            addRules(new DriverOI(leftJoystick, rightJoystick, drive, coralIntake));
        } else {
            final JoystickReader driverGamePad =
                    RobotProvider.instance.getJoystick(InputConstants.DRIVER_GAMEPAD);
            addRules(new DriverOI_GamePad(driverGamePad, drive, coralIntake));
        }

        final JoystickReader boxopGamepad =
                RobotProvider.instance.getJoystick(InputConstants.BOXOP_GAMEPAD);

        addRules(new BoxOpOI(boxopGamepad, algaeIntake, elevator, wrist, climber));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

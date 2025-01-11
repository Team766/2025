package com.team766.robot.reva_2025;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.common.DriverOI;
import com.team766.robot.reva_2025.constants.inputConstants;
import com.team766.robot.reva_2025.procedures.*;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends Procedure {
    private JoystickReader leftJoystick;
    private JoystickReader rightJoystick;
    private JoystickReader boxopGamepad;
    private final DriverOI driverOI;

    public OI() {
        loggerCategory = Category.OPERATOR_INTERFACE;

        leftJoystick = RobotProvider.instance.getJoystick(inputConstants.LEFT_JOYSTICK);
        rightJoystick = RobotProvider.instance.getJoystick(inputConstants.RIGHT_JOYSTICK);
        boxopGamepad = RobotProvider.instance.getJoystick(inputConstants.BOXOP_GAMEPAD);

        driverOI = new DriverOI(Robot.drive, leftJoystick, rightJoystick);
    }

    public void run(final Context context) {
        while (true) {
            // wait for driver station data (and refresh it using the WPILib APIs)
            context.waitFor(() -> RobotProvider.instance.hasNewDriverStationData());
            RobotProvider.instance.refreshDriverStationData();

            // Add driver controls here - make sure to take/release ownership
            driverOI.runOI(context);
            // of mechanisms when appropriate.
        }
    }
}

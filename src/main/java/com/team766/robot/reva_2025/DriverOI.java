package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.hal.JoystickReader;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;

public class DriverOI extends com.team766.robot.common.DriverOI {
    public DriverOI(
            JoystickReader leftJoystick,
            JoystickReader rightJoystick,
            SwerveDrive drive,
            CoralIntake coralIntake) {
        super(leftJoystick, rightJoystick, drive);
        addRule(
                "Outtake Coral",
                leftJoystick.whenButton(InputConstants.BUTTON_CORAL_PLACE),
                ONCE_AND_HOLD,
                coralIntake,
                () -> {
                    coralIntake.out();
                });
    }
}

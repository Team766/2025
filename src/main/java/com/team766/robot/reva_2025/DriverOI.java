package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.ONCE;

import com.team766.hal.JoystickReader;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class DriverOI extends com.team766.robot.common.DriverOI {
    public DriverOI(
            JoystickReader leftJoystick,
            JoystickReader rightJoystick,
            JoystickReader boxopGamepad,
            SwerveDrive drive,
            Wrist wrist) {
        super(leftJoystick, rightJoystick, drive);
        addRule(
                "Wrist to Final Position",
                leftJoystick.whenButton(InputConstants.BUTTON_CORAL_PLACE),
                ONCE,
                wrist,
                () -> {
                    Wrist.WristPosition wristState = wrist.getWristState();
                    if (wristState == Wrist.WristPosition.CORAL_L2_PREP) {
                        wrist.setAngle(Wrist.WristPosition.CORAL_L2_PLACE);
                    }
                    if (wristState == Wrist.WristPosition.CORAL_L3_PREP) {
                        wrist.setAngle(Wrist.WristPosition.CORAL_L3_PLACE);
                    }
                    if (wristState == Wrist.WristPosition.CORAL_L4_PREP) {
                        wrist.setAngle(Wrist.WristPosition.CORAL_L4_PLACE);
                    }
                });
    }
}

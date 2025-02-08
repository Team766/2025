package com.team766.robot.reva_2025;

public class DriverOI extends common.DriverOI {
    public DriverOI(
            JoystickReader leftJoystick,
            JoystickReader rightJoystick,
            SwerveDrive drive,
            Wrist wrist) {
        super(leftJoystick, rightJoystick, drive);
        addRule(
                "Wrist to Final Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_CORAL_PLACE),
                ONCE,
                wrist,
                () -> {
                    WristPosition wristState = wrist.getWristState();
                    if (wristState == wrist.WristPosition.CORAL_L2_PREP) {
                        wrist.setAngle(wrist.WristPosition.CORAL_L2_PLACE);
                    }
                    if (wristState == wrist.WristPosition.CORAL_L3_PREP) {
                        wrist.setAngle(wrist.WristPosition.CORAL_L3_PLACE);
                    }
                    if (wristState == wrist.WristPosition.CORAL_L4_PREP) {
                        wrist.setAngle(wrist.WristPosition.CORAL_L4_PLACE);
                    }
                });
    }
}

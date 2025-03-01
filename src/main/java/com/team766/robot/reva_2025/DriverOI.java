package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.hal.JoystickReader;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.OI.QueuedControl;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.ShootWhenReady;

public class DriverOI extends com.team766.robot.common.DriverOI {
    public DriverOI(
            JoystickReader leftJoystick,
            JoystickReader rightJoystick,
            SwerveDrive drive,
            Elevator elevator,
            Wrist wrist,
            CoralIntake coralIntake,
            AlgaeIntake algaeIntake,
            QueuedControl queuedControl) {
        super(leftJoystick, rightJoystick, drive);
        addRule(
                "Outtake Coral",
                leftJoystick.whenButton(InputConstants.BUTTON_CORAL_PLACE),
                ONCE_AND_HOLD,
                coralIntake,
                () -> {
                    coralIntake.out();
                });
        addRule(
                "Shoot Algae",
                leftJoystick.whenButton(InputConstants.BUTTON_ALGAE_SHOOT),
                ONCE_AND_HOLD,
                () -> new ShootWhenReady(algaeIntake));
        }
}

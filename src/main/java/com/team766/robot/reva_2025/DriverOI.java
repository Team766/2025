package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.hal.JoystickReader;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.OI.QueuedControl;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.ShootWhenReady;
import java.util.Set;

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
                        Set.of(coralIntake, wrist),
                        () -> {
                            if (queuedControl.scoreHeight == ScoreHeight.L4) {
                                wrist.nudge(1);
                            }
                            coralIntake.out();
                        })
                .withFinishedTriggeringProcedure(coralIntake, () -> coralIntake.stop());
        addRule(
                "Shoot Algae",
                leftJoystick.whenButton(InputConstants.BUTTON_ALGAE_SHOOT),
                ONCE_AND_HOLD,
                () -> new ShootWhenReady(algaeIntake));

        addRule(
                "Algae Intake to L2 L3",
                () -> leftJoystick.getPOV() == InputConstants.BUTTON_JOYSTICK_ALGAE_INTAKE_L2_L3,
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.L2L3AlgaeIntake);
                });

        addRule(
                "Algae Intake to L3 L4",
                () -> leftJoystick.getPOV() == InputConstants.BUTTON_JOYSTICK_ALGAE_INTAKE_L3_L4,
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.L3L4AlgaeIntake);
                });
        addRule(
                "Algae Intake to Stow",
                leftJoystick.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_STOW),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.Stow);
                });

        addRule(
                "Nudge Wrist Up",
                rightJoystick.whenPOV(0),
                ONCE_AND_HOLD,
                wrist,
                () -> {
                    wrist.nudge(-1);
                });

        addRule(
                "Nudge Wrist Down",
                rightJoystick.whenPOV(180),
                ONCE_AND_HOLD,
                wrist,
                () -> {
                    wrist.nudge(1);
                });
    }
}

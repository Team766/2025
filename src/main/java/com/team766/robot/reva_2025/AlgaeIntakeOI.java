package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;

public class AlgaeIntakeOI extends RuleGroup {
    public AlgaeIntakeOI(JoystickReader boxopGamepad, AlgaeIntake algaeIntake) {
        addRule(
                        "In for Intake",
                        boxopGamepad.whenButton(InputConstants.BUTTON_IN_INTAKE),
                        ONCE_AND_HOLD,
                        algaeIntake,
                        () -> {
                            algaeIntake.in();
                        })
                .withFinishedTriggeringProcedure(
                        algaeIntake,
                        () -> {
                            algaeIntake.stop();
                        });

        addRule(
                        "Out for Intake",
                        boxopGamepad.whenButton(InputConstants.BUTTON_OUT_INTAKE),
                        ONCE_AND_HOLD,
                        algaeIntake,
                        () -> {
                            algaeIntake.out();
                        })
                .withFinishedTriggeringProcedure(
                        algaeIntake,
                        () -> {
                            algaeIntake.stop();
                        });

        addRule(
                " Stow for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_STOW_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.Stow);
                });

        addRule(
                " Ground for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_GROUND_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.GroundIntake);
                });

        addRule(
                " Level 2/3 for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_LEVEL23_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.L2L3AlgaeIntake);
                });

        addRule(
                " Level 3/4 for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_LEVEL34_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.L3L4AlgaeIntake);
                });

        addRule(
                        " Shooter On for Shooter",
                        boxopGamepad.whenButton(InputConstants.BUTTON_ON_SHOOTER),
                        ONCE_AND_HOLD,
                        algaeIntake,
                        () -> {
                            algaeIntake.shooterOn();
                        })
                .withFinishedTriggeringProcedure(
                        algaeIntake,
                        () -> {
                            algaeIntake.shooterOff();
                        });
    }
}

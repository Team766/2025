package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;

public class AlgaeIntakeOI extends RuleGroup {
    public AlgaeIntakeOI (
        JoystickReader leftJoystick,
        JoystickReader rightJoystick,
        AlgaeIntake algaeIntake ){
            addRule(
                "In for Intake",
                leftJoystick.whenButton(InputConstants.BUTTON_IN_INTAKE),
                REPEATEDLY,
                algaeIntake,
                () -> {
                    algaeIntake.in();
                }
            );

            addRule(
                "Out for Intake",
                leftJoystick.whenButton(InputConstants.BUTTON_OUT_INTAKE),
                REPEATEDLY,
                algaeIntake,
                () -> {
                    algaeIntake.out();
                }

            );
            //Ask someone(probably Raj) about how to implement stop in Intake code
            //Ask Raj about arm angle

            addRule(
                " Stow for Level",
                rightJoystick.whenButton(InputConstants.BUTTON_STOW_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle();
                }

            );


            addRule(
                " Ground for Level",
                rightJoystick.whenButton(InputConstants.BUTTON_GROUND_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle();
                }

            );

            addRule(
                " Level 2/3 for Level",
                rightJoystick.whenButton(InputConstants.BUTTON_LEVEL23_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle();
                }

            );

            addRule(
                " Level 3/4 for Level",
                rightJoystick.whenButton(InputConstants.BUTTON_LEVEL34_LEVEL),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle();
                }

            );

            addRule(
                " Shooter On for Shooter",
                leftJoystick.whenButton(InputConstants.BUTTON_ON_SHOOTER),
                REPEATEDLY,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle();
                }

            );
        }
}

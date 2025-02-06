package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.Conditions.LogicalAnd;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class BoxOpOI extends RuleGroup {
    public BoxOpOI(
            JoystickReader boxopGamepad, AlgaeIntake algaeIntake, Elevator elevator, Wrist wrist) {

        // ALGAE INTAKE & SHOOTER

        addRule(
                "In for Intake",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_TO_SHOOTER),
                ONCE_AND_HOLD,
                algaeIntake,
                () -> {
                    algaeIntake.in();
                });

        addRule(
                "Out for Intake",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_SHOOTER),
                ONCE_AND_HOLD,
                algaeIntake,
                () -> {
                    algaeIntake.out();
                });

        addRule(
                " Shoot for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_SHOOTER),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.Shoot);
                });

        addRule(
                " Ground for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_GROUND),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.GroundIntake);
                });

        addRule(
                " Level 2 for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_L2),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.L2AlgaeIntake);
                });

        addRule(
                " Level 3 for Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_L3),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.L3AlgaeIntake);
                });

        /*
        addRule(
                        " Shooter On for Shooter",
                        boxopGamepad.whenButton(InputConstants.BUTTON_ON_SHOOTER),
                        ONCE_AND_HOLD,
                        algaeIntake,
                        () -> {
                            algaeIntake.shooterOn();
                        });
        */

        // ELEVATOR

        addRule(
                "Move Elevator L1",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L1),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
                ONCE,
                elevator,
                () -> {
                    elevator.setPosition(Elevator.Position.ELEVATOR_L1);
                });

        addRule(
                "Move Elevator L2",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L2),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
                ONCE,
                elevator,
                () -> {
                    elevator.setPosition(Elevator.Position.ELEVATOR_L2);
                    wrist.setAngle(Wrist.WristPosition.CORAL_L2_PREP);
                });
        addRule(
                "Move Elevator L3",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L3),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
                ONCE,
                elevator,
                () -> {
                    elevator.setPosition(Elevator.Position.ELEVATOR_L3);
                    wrist.setAngle(Wrist.WristPosition.CORAL_L3_PREP);
                });
        addRule(
                "Move Elevator L4",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L4),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
                ONCE,
                elevator,
                () -> {
                    elevator.setPosition(Elevator.Position.ELEVATOR_L4);
                    wrist.setAngle(Wrist.WristPosition.CORAL_L4_PREP);
                });
    }
}

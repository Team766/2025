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
            JoystickReader boxopGamepad,
            AlgaeIntake algaeIntake,
            Elevator elevator,
            Wrist wrist,
            Climber climber) {

        // ALGAE INTAKE & SHOOTER

        addRule(
                "Algae Intake to Shooter",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_TO_SHOOTER),
                ONCE_AND_HOLD,
                algaeIntake,
                () -> {
                    algaeIntake.in();
                });

        addRule(
                "Algae Outtake (shooter?)",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_SHOOTER),
                ONCE_AND_HOLD,
                algaeIntake,
                () -> {
                    algaeIntake.out();
                });

        addRule(
                "Algae to Shooting Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_SHOOTER),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.Shoot);
                });

        addRule(
                "Algae Intake to Ground Level",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_GROUND),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.GroundIntake);
                });

        addRule(
                "Algae Intake to Level 2",
                boxopGamepad.whenButton(InputConstants.BUTTON_ALGAE_INTAKE_L2),
                ONCE,
                algaeIntake,
                () -> {
                    algaeIntake.setArmAngle(Level.L2AlgaeIntake);
                });

        addRule(
                "Algae Intake to Level 3",
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
                "Move Elevator L1 (Intake?)",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L1),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
                ONCE,
                elevator,
                () -> {
                    elevator.setPosition(Elevator.Position.ELEVATOR_L1);
                    wrist.setAngle(Wrist.WristPosition.CORAL_INTAKE);
                });

        addRule(
                "Move Elevator & Wrist L2",
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
                "Move Elevator & Wrist L3",
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
                "Move Elevator & Wrist L4",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L4),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
                ONCE,
                elevator,
                () -> {
                    elevator.setPosition(Elevator.Position.ELEVATOR_L4);
                    wrist.setAngle(Wrist.WristPosition.CORAL_L4_PREP);
                });
        addRule(
                "Nudge Elevator Up",
                boxopGamepad.whenAxisMoved(InputConstants.AXIS_ELEVATOR_FINETUNE),
                ONCE_AND_HOLD,
                elevator,
                () -> {
                    elevator.nudgeUp(boxopGamepad.getAxis(InputConstants.AXIS_ELEVATOR_FINETUNE));
                });
        addRule(
                "Nudge Elevator Down",
                boxopGamepad.whenAxisMoved(InputConstants.AXIS_ELEVATOR_FINETUNE),
                ONCE_AND_HOLD,
                elevator,
                () -> {
                    elevator.nudgeDown(boxopGamepad.getAxis(InputConstants.AXIS_ELEVATOR_FINETUNE));
                });

        // WRIST

        addRule(
                "Nudge Wrist Up",
                boxopGamepad.whenAxisMoved(InputConstants.AXIS_WRIST_FINETUNE),
                ONCE_AND_HOLD,
                wrist,
                () -> {
                    wrist.nudgeUp(boxopGamepad.getAxis(InputConstants.AXIS_WRIST_FINETUNE));
                });
        addRule(
                "Nudge Wrist Down",
                boxopGamepad.whenAxisMoved(InputConstants.AXIS_WRIST_FINETUNE),
                ONCE_AND_HOLD,
                wrist,
                () -> {
                    wrist.nudgeDown(boxopGamepad.getAxis(InputConstants.AXIS_WRIST_FINETUNE));
                });

        // CLIMBER

        addRule(
                "Climber Up",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_CLIMB),
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON)),
                ONCE,
                climber,
                () -> {
                    climber.climbUp();
                });
        addRule(
                "Climber Down",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_CLIMB),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
                ONCE,
                climber,
                () -> {
                    climber.climbDown();
                });
    }
}

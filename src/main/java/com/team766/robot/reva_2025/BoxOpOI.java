package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.Conditions.LogicalAnd;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Elevator.Position;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;
import java.util.Set;

public class BoxOpOI extends RuleGroup {
    private Elevator.Position targetElevatorPosition;
    private Wrist.WristPosition targetWristPosition;
    private AlgaeIntake.Level targetAlgaeLevel;

    public BoxOpOI(
            JoystickReader boxopGamepad,
            AlgaeIntake algaeIntake,
            Elevator elevator,
            Wrist wrist,
            Climber climber,
            CoralIntake coralIntake) {

        boxopGamepad.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);

        targetElevatorPosition = Elevator.Position.ELEVATOR_BOTTOM;
        targetWristPosition = Wrist.WristPosition.CORAL_INTAKE;
        targetAlgaeLevel = AlgaeIntake.Level.GroundIntake;
        // ALGAE INTAKE POSITIONS

        addRule(
                "Algae Intake to Stow",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_STOW,
                ONCE,
                algaeIntake,
                () -> {
                    targetAlgaeLevel = Level.Stow;
                });

        addRule(
                "Algae Intake to Ground Level",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_GROUND,
                ONCE,
                algaeIntake,
                () -> {
                    targetAlgaeLevel = Level.GroundIntake;
                });

        addRule(
                "Algae Intake to L2/L3",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_L2_L3,
                ONCE,
                algaeIntake,
                () -> {
                    targetAlgaeLevel = Level.L2L3AlgaeIntake;
                });

        addRule(
                "Algae Intake to L3/L4",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_L3_L4,
                ONCE,
                algaeIntake,
                () -> {
                    targetAlgaeLevel = Level.L3L4AlgaeIntake;
                });

        addRule(
                        "Move Algae Intake to TargetLevel",
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_LEFT_BUMPER_BUTTON),
                        ONCE,
                        algaeIntake,
                        () -> {
                            algaeIntake.setArmAngle(targetAlgaeLevel);
                        })
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Algae Motors to Intake Power",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                        ONCE_AND_HOLD,
                                        algaeIntake,
                                        () -> {
                                            algaeIntake.setState(AlgaeIntake.State.In);
                                        });

                                addRule(
                                        "Algae Motors to Outtake Power",
                                        new LogicalAnd(
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants
                                                                .BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                                boxopGamepad.whenButton(
                                                        InputConstants.GAMEPAD_START_BUTTON)),
                                        ONCE_AND_HOLD,
                                        algaeIntake,
                                        () -> {
                                            algaeIntake.setState(AlgaeIntake.State.Out);
                                        });
                            }
                        })
                .withFinishedTriggeringProcedure(
                        algaeIntake,
                        () -> {
                            algaeIntake.setArmAngle(Level.Stow);
                        });

        // ALGAE INTAKE MOTOR CONTROLS / SHOOTING

        addRule(
                "Spin Up Algae Shooter Motors",
                boxopGamepad.whenAxisMoved(InputConstants.BUTTON_ALGAE_SHOOTER_ON),
                ONCE_AND_HOLD,
                algaeIntake,
                () -> {
                    algaeIntake.setState(AlgaeIntake.State.Shoot);
                    algaeIntake.setArmAngle(Level.Shoot);
                });

        // ELEVATOR

        addRule(
                "Set Elevator/Wrist Intake",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L1),
                ONCE,
                Set.of(elevator, wrist),
                () -> {
                    targetElevatorPosition = Elevator.Position.ELEVATOR_INTAKE;
                    targetWristPosition = Wrist.WristPosition.CORAL_INTAKE;
                });
        addRule(
                "Set Elevator/Wrist L2",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L2),
                ONCE,
                Set.of(elevator, wrist),
                () -> {
                    targetElevatorPosition = Elevator.Position.ELEVATOR_L3;
                    targetWristPosition = Wrist.WristPosition.CORAL_L3_PLACE;
                });

        addRule(
                "Set Elevator/Wrist L3",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L3),
                ONCE,
                Set.of(elevator, wrist),
                () -> {
                    targetElevatorPosition = Elevator.Position.ELEVATOR_L4;
                    targetWristPosition = Wrist.WristPosition.CORAL_L4_PLACE;
                });

        addRule(
                "Set Elevator/Wrist L4",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L4),
                ONCE,
                Set.of(elevator, wrist),
                () -> {
                    targetElevatorPosition = Elevator.Position.ELEVATOR_L1;
                    targetWristPosition = Wrist.WristPosition.CORAL_L1_PLACE;
                });

        addRule(
                        "Move Elevator & Wrist to TargetPosition",
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                        ONCE_AND_HOLD,
                        Set.of(elevator, wrist),
                        () -> {
                            elevator.setPosition(targetElevatorPosition);
                            wrist.setAngle(targetWristPosition);
                        })
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Grabber Motor to Intake Power",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                        ONCE_AND_HOLD,
                                        coralIntake,
                                        () -> {
                                            coralIntake.in();
                                        });

                                addRule(
                                        "Grabber Motor to Outtake Power",
                                        new LogicalAnd(
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants
                                                                .BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                                boxopGamepad.whenButton(
                                                        InputConstants.GAMEPAD_START_BUTTON)),
                                        ONCE_AND_HOLD,
                                        coralIntake,
                                        () -> {
                                            coralIntake.out();
                                        });
                            }
                        })
                .withFinishedTriggeringProcedure(
                        Set.of(elevator, wrist),
                        () -> {
                            elevator.setPosition(Position.ELEVATOR_BOTTOM);
                            wrist.setAngle(WristPosition.CORAL_INTAKE);
                        });

        // FINE TUNING

        addRule(
                "Nudge Elevator",
                new LogicalAnd(
                        boxopGamepad.whenAxisMoved(InputConstants.AXIS_ELEVATOR_FINETUNE),
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON)),
                ONCE_AND_HOLD,
                elevator,
                () -> {
                    elevator.nudge(boxopGamepad.getAxis(InputConstants.AXIS_ELEVATOR_FINETUNE));
                });

        addRule(
                "Nudge Wrist",
                new LogicalAnd(
                        boxopGamepad.whenAxisMoved(InputConstants.AXIS_WRIST_FINETUNE),
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON)),
                ONCE_AND_HOLD,
                wrist,
                () -> {
                    wrist.nudge(boxopGamepad.getAxis(InputConstants.AXIS_WRIST_FINETUNE));
                });

        addRule(
                "Nudge Algae",
                new LogicalAnd(
                        boxopGamepad.whenAxisMoved(InputConstants.AXIS_ALGAE_FINETUNE),
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_LEFT_BUMPER_BUTTON)),
                ONCE_AND_HOLD,
                algaeIntake,
                () -> {
                    algaeIntake.nudge(boxopGamepad.getAxis(InputConstants.AXIS_ALGAE_FINETUNE));
                });

        // CLIMBER

        addRule(
                "Climber Up/Down",
                new LogicalAnd(
                        boxopGamepad.whenButton(InputConstants.BUTTON_CLIMB),
                        boxopGamepad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS)),
                ONCE_AND_HOLD,
                climber,
                () -> {
                    climber.climb(boxopGamepad.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_YAXIS));
                });
    }
}

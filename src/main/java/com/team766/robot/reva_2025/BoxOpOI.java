package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.Conditions;
import com.team766.framework3.Conditions.LogicalAnd;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.reva_2025.OI.QueuedControl;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;
import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;
import com.team766.robot.reva_2025.procedures.HoldAlgae;
import com.team766.robot.reva_2025.procedures.IntakeAlgae;
import java.util.Set;

public class BoxOpOI extends RuleGroup {

    public BoxOpOI(
            JoystickReader boxopGamepad,
            AlgaeIntake algaeIntake,
            Elevator elevator,
            Wrist wrist,
            Climber climber,
            CoralIntake coralIntake,
            QueuedControl queuedControl) {

        boxopGamepad.setAllAxisDeadzone(ControlConstants.GAMEPAD_DEADZONE);

        // CLIMBER

        addRule(
                        "Control Climber",
                        new Conditions.Toggle(
                                () -> boxopGamepad.getButtonPressed(InputConstants.BUTTON_CLIMB)),
                        ONCE_AND_HOLD,
                        Set.of(algaeIntake, wrist, elevator),
                        () -> {
                            algaeIntake.setArmAngle(Level.GroundIntake);
                            elevator.setPosition(ElevatorPosition.ELEVATOR_CLIMB);
                            wrist.setAngle(WristPosition.CORAL_CLIMB);
                        })
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Move Climber Up or Down",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.GAMEPAD_RIGHT_STICK_YAXIS),
                                        ONCE_AND_HOLD,
                                        climber,
                                        () ->
                                                climber.climb(
                                                        boxopGamepad.getAxis(
                                                                InputConstants
                                                                        .GAMEPAD_RIGHT_STICK_YAXIS)));
                            }
                        });

        // ALGAE INTAKE POSITIONS

        addRule(
                "Queue Algae Intake to Stow Position",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_STOW,
                ONCE,
                algaeIntake,
                () -> {
                    queuedControl.algaeLevel = Level.Stow;
                });

        addRule(
                "Queue Algae Intake to Ground Intake Position",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_GROUND,
                ONCE,
                algaeIntake,
                () -> {
                    queuedControl.algaeLevel = Level.GroundIntake;
                });

        addRule(
                "Queue Algae Intake to L2 L3 Position",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_L2_L3,
                ONCE,
                algaeIntake,
                () -> {
                    queuedControl.algaeLevel = Level.L2L3AlgaeIntake;
                });

        addRule(
                "Queue Algae Intake to L3 L4 Position",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_L3_L4,
                ONCE,
                Set.of(elevator, wrist, algaeIntake),
                () -> {
                    if (queuedControl.scoreHeight == ScoreHeight.L2) {
                        queuedControl.scoreHeight = ScoreHeight.Intake;
                    }
                    queuedControl.algaeLevel = Level.L3L4AlgaeIntake;
                });

        addRule(
                        "Move Algae Intake to Target Position",
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_LEFT_BUMPER_BUTTON),
                        ONCE,
                        algaeIntake,
                        () -> {
                            algaeIntake.setArmAngle(queuedControl.algaeLevel);
                        })
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Spin Algae Intake Motor Out",
                                        new LogicalAnd(
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants
                                                                .BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                                boxopGamepad.whenButton(
                                                        InputConstants.GAMEPAD_START_BUTTON)),
                                        ONCE_AND_HOLD,
                                        Set.of(elevator, wrist, coralIntake, algaeIntake),
                                        () -> {
                                            algaeIntake.setState(AlgaeIntake.State.Out);
                                        });

                                addRule(
                                        "Spin Algae Intake Motor In",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                        ONCE_AND_HOLD,
                                        Set.of(elevator, wrist, coralIntake, algaeIntake),
                                        context -> {
                                            if (queuedControl.algaeLevel == Level.GroundIntake) {
                                                algaeIntake.setState(State.HoldAlgae);
                                            } else {
                                                elevator.setPosition(ElevatorPosition.ELEVATOR_INTAKE);
                                                context.runSync(
                                                        new IntakeAlgae(
                                                                algaeIntake,
                                                                queuedControl.algaeLevel));
                                            }
                                        });
                                addRule(
                                        "Nudge Algae",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.AXIS_ALGAE_FINETUNE),
                                        ONCE_AND_HOLD,
                                        algaeIntake,
                                        () -> {
                                            algaeIntake.nudge(
                                                    boxopGamepad.getAxis(
                                                            InputConstants.AXIS_ALGAE_FINETUNE));
                                        });
                            }
                        })
                .withFinishedTriggeringProcedure(
                        algaeIntake,
                        context -> {
                            // make sure we don't squish an algae
                            var status = getStatus(AlgaeIntake.AlgaeIntakeStatus.class);
                            if (status.isPresent()
                                    && status.get().intakeProximity().isPresent()
                                    && status.get().level() != Level.Stow) {
                                if (status.get().level() == Level.L2L3AlgaeIntake
                                        || status.get().level() == Level.L3L4AlgaeIntake) {
                                    context.runSync(new HoldAlgae(algaeIntake));
                                } else {
                                    algaeIntake.setArmAngle(Level.GroundIntake);
                                }
                            } else {
                                algaeIntake.setArmAngle(Level.Stow);
                            }
                        });

        // ALGAE INTAKE MOTOR CONTROLS / SHOOTING

        addRule(
                "Spin Up Algae Shooter Motor",
                boxopGamepad.whenAxisMoved(InputConstants.BUTTON_ALGAE_SHOOTER_ON),
                ONCE_AND_HOLD,
                algaeIntake,
                () -> {
                    algaeIntake.setState(AlgaeIntake.State.Shoot);
                    algaeIntake.setArmAngle(Level.Shoot);
                });

        // ELEVATOR AND WRIST

        addRule(
                        "Grabber Motor Auto Intake",
                        boxopGamepad.whenAxisMoved(InputConstants.BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                        ONCE_AND_HOLD,
                        Set.of(elevator, wrist, coralIntake),
                        () -> {
                            elevator.setPosition(ElevatorPosition.ELEVATOR_INTAKE);
                            wrist.setAngle(WristPosition.CORAL_INTAKE);
                            coralIntake.in();
                        })
                .withFinishedTriggeringProcedure(
                        Set.of(
                                elevator,
                                wrist,
                                coralIntake,
                                algaeIntake), // reserves algae mechanism so that control for
                        // intaking algae is higher priority
                        () -> {
                            elevator.setPosition(ElevatorPosition.ELEVATOR_BOTTOM);
                            wrist.setAngle(WristPosition.CORAL_INTAKE);
                            coralIntake.idle();
                        });

        addRule(
                "Queue Elevator and Wrist to L1 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L1),
                ONCE,
                Set.of(elevator, wrist),
                () -> {
                    queuedControl.scoreHeight = ScoreHeight.L1;
                });
        addRule(
                "Queue Elevator and Wrist to L2 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L2),
                ONCE,
                Set.of(elevator, wrist, algaeIntake),
                () -> {
                    if (queuedControl.algaeLevel == Level.L3L4AlgaeIntake) {
                        queuedControl.algaeLevel = Level.Stow;
                    }
                    queuedControl.scoreHeight = ScoreHeight.L2;
                });
        addRule(
                "Queue Elevator and Wrist to L3 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L3),
                ONCE,
                Set.of(elevator, wrist),
                () -> {
                    queuedControl.scoreHeight = ScoreHeight.L3;
                });

        addRule(
                "Queue Elevator and Wrist to L4 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L4),
                ONCE,
                Set.of(elevator, wrist),
                () -> {
                    queuedControl.scoreHeight = ScoreHeight.L4;
                });

        addRule(
                        "Move Elevator and Wrist to Target Position",
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                        ONCE,
                        Set.of(elevator, wrist),
                        () -> {
                            elevator.setPosition(queuedControl.scoreHeight.getElevatorPosition());
                            wrist.setAngle(queuedControl.scoreHeight.getWristPosition());
                        })
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Nudge Elevator",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.AXIS_ELEVATOR_FINETUNE),
                                        ONCE_AND_HOLD,
                                        elevator,
                                        () -> {
                                            elevator.nudge(
                                                    boxopGamepad.getAxis(
                                                            InputConstants.AXIS_ELEVATOR_FINETUNE));
                                        });

                                addRule(
                                        "Nudge Wrist",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.AXIS_WRIST_FINETUNE),
                                        ONCE_AND_HOLD,
                                        wrist,
                                        () -> {
                                            wrist.nudge(
                                                    boxopGamepad.getAxis(
                                                            InputConstants.AXIS_WRIST_FINETUNE));
                                        });
                            }
                        })
                .withFinishedTriggeringProcedure(
                        Set.of(elevator, wrist),
                        () -> {
                            elevator.setPosition(ElevatorPosition.ELEVATOR_BOTTOM);
                            wrist.setAngle(WristPosition.CORAL_INTAKE);
                        });
    }
}

package com.team766.robot.copy_2910;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.Conditions;
import com.team766.framework.Conditions.LogicalAnd;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.copy_2910.OI.QueuedControl;
import com.team766.robot.copy_2910.mechanisms.Climber;
import com.team766.robot.copy_2910.mechanisms.Elevator;
import com.team766.robot.copy_2910.mechanisms.Intake;
import com.team766.robot.copy_2910.mechanisms.Shoulder;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import com.team766.robot.copy_2910.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.copy_2910.mechanisms.Shoulder.ShoulderPosition;
import com.team766.robot.copy_2910.mechanisms.Wrist.WristPosition;
import com.team766.robot.copy_2910.procedures.IntakeCoral;
import com.team766.robot.copy_2910.procedures.MoveWristvator;

import java.util.Set;

public class BoxOpOI extends RuleGroup {

    public BoxOpOI(
            JoystickReader boxopGamepad,
            Shoulder shoulder,
            Elevator elevator,
            Wrist wrist,
            Climber climber,
            Intake intake,
            QueuedControl queuedControl) {

        boxopGamepad.setAllAxisDeadzone(ControlConstants.GAMEPAD_DEADZONE);

        // CLIMBER

        addRule(
                        "Control Climber",
                        new Conditions.Toggle(boxopGamepad.whenButton(InputConstants.BUTTON_CLIMB)),
                        ONCE_AND_HOLD,
                        Set.of(climber, wrist, elevator, shoulder),
                        () -> {
                            elevator.setPosition(Elevator.ElevatorPosition.MAXIMUM);
                            wrist.setPosition(Wrist.WristPosition.ALGAE_LOW);
                            climber.setClimberSpeed(0.5);
                            shoulder.setPosition(Shoulder.ShoulderPosition.CLIMBER);
                        })
                .withFinishedTriggeringProcedure(
                        Set.of(climber, shoulder),
                        context -> {
                            climber.stop();
                            shoulder.setPosition(ShoulderPosition.CORAL_GROUND);
                            log("finished triggering");
                        });
        // ALGAE INTAKE POSITIONS


        addRule(
                "Queue to Algae Ground Intake Position",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_GROUND,
                ONCE,
                Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.ALGAE_GROUND;
                    queuedControl.shoulderPosition = ShoulderPosition.ALGAE_GROUND;
                    queuedControl.wristPosition = WristPosition.ALGAE_GROUND;
                });

        addRule(
                "Queue Algae Intake to L2 L3 Position",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_L2_L3,
                ONCE,
                Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.ALGAE_LOW;
                    queuedControl.shoulderPosition = ShoulderPosition.ALGAE_LOW;
                    queuedControl.wristPosition = WristPosition.ALGAE_LOW;
                });

        addRule(
                "Queue Algae Intake to L3 L4 Position",
                () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_L3_L4,
                ONCE,
                Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.ALGAE_LOW;
                    queuedControl.shoulderPosition = ShoulderPosition.ALGAE_LOW;
                    queuedControl.wristPosition = WristPosition.ALGAE_LOW;
                });
        
        addRule(
                "Ground Intake",
                boxopGamepad.whenAxisMoved(InputConstants.BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                ONCE_AND_HOLD,
                () -> new IntakeCoral(intake, elevator, shoulder, wrist)).withFinishedTriggeringProcedure(
                    Set.of(intake, elevator, wrist, shoulder),
                    () -> {
                        intake.stop();
                        elevator.setPosition(ElevatorPosition.READY);
                        wrist.setPosition(WristPosition.STOW);
                        shoulder.setPosition(ShoulderPosition.STOW);
                    }
                );

        addRule(
                "Queue Elevator and Wrist to L1 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L1),
                ONCE,
                Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.L1;
                    queuedControl.shoulderPosition = ShoulderPosition.L1;
                    queuedControl.wristPosition = WristPosition.L1;
                });

        addRule(
                "Queue Elevator and Wrist to L2 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L2),
                ONCE,
                Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.L2;
                    queuedControl.shoulderPosition = ShoulderPosition.L2;
                    queuedControl.wristPosition = WristPosition.L2;
                });

        addRule(
                "Queue Elevator and Wrist to L3 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L3),
                ONCE,
                Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.L3;
                    queuedControl.shoulderPosition = ShoulderPosition.L3;
                    queuedControl.wristPosition = WristPosition.L3;
                });

        addRule(
                "Queue Elevator and Wrist to L4 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L4),
                ONCE,
                Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.L4;
                    queuedControl.shoulderPosition = ShoulderPosition.L4;
                    queuedControl.wristPosition = WristPosition.L4;
                });

        addRule(
                "Move to Target Position",
                boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                ONCE_AND_HOLD,
                () -> new MoveWristvator(shoulder, elevator, wrist, queuedControl.shoulderPosition, queuedControl.elevatorPosition, queuedControl.wristPosition))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Spin Algae Intake Motor In",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                        ONCE_AND_HOLD,
                                        Set.of(intake),
                                        context -> {
                                            intake.setAlgaePower(0.5);
                                        }).withFinishedTriggeringProcedure(
                                            intake,
                                            () -> intake.setAlgaePower(0.1)
                                        );
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

                                addRule(
                                    "Nudge Shoulder",
                                    boxopGamepad.whenAxisMoved(
                                            InputConstants.AXIS_WRIST_FINETUNE),
                                    ONCE_AND_HOLD,
                                    wrist,
                                    () -> {
                                        shoulder.nudge(
                                                boxopGamepad.getAxis(
                                                        InputConstants.AXIS_WRIST_FINETUNE));
                                    });
                            }
                        })
                .withFinishedTriggeringProcedure(
                    Set.of(elevator, wrist, shoulder),
                    () -> {
                        elevator.setPosition(ElevatorPosition.READY);
                        wrist.setPosition(WristPosition.STOW);
                        shoulder.setPosition(ShoulderPosition.STOW);
                    });
    }
}

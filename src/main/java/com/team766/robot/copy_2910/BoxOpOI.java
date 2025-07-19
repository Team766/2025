package com.team766.robot.copy_2910;
// first time every coding in java 
// dont stone me to much for doing nothing correctly
//yes I copyied everything no it probably doesent work
import static com.team766.framework.RulePersistence.*;

import com.team766.framework.Conditions;
import com.team766.framework.Conditions.LogicalAnd;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.reva_2025.OI.QueuedControl; //idk should work
//import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight; not useful
import com.team766.robot.reva_2025.constants.InputConstants; // for joystick readings 
import com.team766.robot.copy_2910.mechanisms.Elevator;
//import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level; no clue
//import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State; no clue
import com.team766.robot.copy_2910.mechanisms.Intake;
import com.team766.robot.copy_2910.mechanisms.Shoulder;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import com.team766.robot.copy_2910.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.copy_2910.mechanisms.Wrist.WristPosition;
import com.team766.robot.copy_2910.procedures.IntakeCoral;
import com.team766.robot.copy_2910.procedures.OuttakeCoral;
import java.util.Set;


public class BoxOpOI extends RuleGroup {

    public BoxOpOI(
            JoystickReader boxopGamepad,
            Elevator elevator,
            Wrist wrist,
            Intake intake,
            Shoulder shoulder,
            QueuedControl queuedControl) {

        boxopGamepad.setAllAxisDeadzone(ControlConstants.GAMEPAD_DEADZONE);

// ELEVATOR, WRIST, AND SHOULDER

        addRule(
                        "Grabber Motor Auto Intake",
                        boxopGamepad.whenAxisMoved(InputConstants.BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                        ONCE_AND_HOLD,
                        Set.of(elevator, wrist, intake, shoulder),
                        () -> {
                            elevator.setPosition(ElevatorPosition.ELEVATOR_INTAKE);
                            wrist.setAngle(WristPosition.CORAL_INTAKE);
                            shoulder.setAngle(ShoulderPosition.CORAL_INTAKE);
                            intake.in();
                        })
                .withFinishedTriggeringProcedure(
                        Set.of(
                                elevator,
                                wrist,
                                intake,
                                shoulder,
                                ), 
                        () -> {
                            elevator.setPosition(ElevatorPosition.ELEVATOR_BOTTOM);
                            wrist.setAngle(WristPosition.CORAL_INTAKE);
                            shoulder.setAngle(ShoulderPosition.CoralIntake);
                            intake.idle();
                        });

        addRule(
                "Queue Elevator and Wrist to L1 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L1),
                ONCE,
                Set.of(elevator, wrist, shoulder),
                () -> {
                    queuedControl.scoreHeight = ScoreHeight.L1;
                });
        addRule(
                "Queue Elevator and Wrist to L2 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L2),
                ONCE,
                Set.of(elevator, wrist, shoulder),
                () -> {
                   
                    queuedControl.scoreHeight = ScoreHeight.L2;
                });
        addRule(
                "Queue Elevator and Wrist to L3 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L3),
                ONCE,
                Set.of(elevator, wrist, shoulder),
                () -> {
                    queuedControl.scoreHeight = ScoreHeight.L3;
                });

        addRule(
                "Queue Elevator and Wrist to L4 Position",
                boxopGamepad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L4),
                ONCE,
                Set.of(elevator, wrist, shoulder),
                () -> {
                    queuedControl.scoreHeight = ScoreHeight.L4;
                });

        addRule(
                        "Move Elevator and Wrist to Target Position",
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                        ONCE,
                        Set.of(elevator, wrist, shoulder),
                        () -> {
                            elevator.setPosition(queuedControl.scoreHeight.getElevatorPosition());
                            wrist.setAngle(queuedControl.scoreHeight.getWristPosition());
                            shoulder.setAngle(queuedControl.scoreHeight.getShoulderPosition());
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
                        Set.of(elevator, wrist, shoulder),
                        () -> {
                            elevator.setPosition(ElevatorPosition.ELEVATOR_BOTTOM);
                            wrist.setAngle(WristPosition.CORAL_INTAKE);
                           shoulder.setAngle(getShoulderPosition.STOW);
                        
                        });
    




            }
}
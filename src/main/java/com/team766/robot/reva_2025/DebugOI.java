package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
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

/**
 * Programmer-centric controls to test each of our (non-drive) mechanisms.
 * Useful for tuning and for testing, eg in the pit.
 *
 * Uses a DOIO KB16 macropad, as follows:
 *
 *
 *      ┌───┬───┬───┬───┐   12<──  12<──
 *      │ 1 │ 2 │ 3 │ 4 │   ( 3 )  ( 4 )
 *      ├───┼───┼───┼───┤    -─>8  -─>8
 *      │ 5 │ 6 │ 7 │ 8 │
 *      ├───┼───┼───┼───┤
 *      │ 9 │ 10| 11│ 12|     12<──
 *      ├───┼───┼───┼───┤      (   )
 *      │ 13│ 14│ 15│ 16│      -─>8
 *      └───┴───┴───┴───┘
 *
 */
public class DebugOI extends RuleGroup {
    public DebugOI(
            JoystickReader macropad,
            Climber climber,
            Elevator elevator,
            Wrist wrist,
            AlgaeIntake algae,
            CoralIntake coral) {

        addRule("Debug Control Algae", macropad.whenButton(InputConstants.CONTROL_ALGAE))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                                "Debug Algae Nudge No PID",
                                                macropad.whenButton(InputConstants.NUDGE_NO_PID))
                                        .whenTriggering(
                                                new RuleGroup() {
                                                    {
                                                        addRule(
                                                                        "Debug Algae Nudge No PID Up",
                                                                        macropad.whenButton(
                                                                                InputConstants
                                                                                        .NUDGE_UP),
                                                                        algae,
                                                                        () -> algae.nudgeNoPID(0.2))
                                                                .withFinishedTriggeringProcedure(
                                                                        algae,
                                                                        () -> algae.nudgeNoPID(0));
                                                        addRule(
                                                                        "Debug Algae Nudge No PID Down",
                                                                        macropad.whenButton(
                                                                                InputConstants
                                                                                        .NUDGE_DOWN),
                                                                        algae,
                                                                        () ->
                                                                                algae.nudgeNoPID(
                                                                                        -0.2))
                                                                .withFinishedTriggeringProcedure(
                                                                        algae,
                                                                        () -> algae.nudgeNoPID(0));
                                                    }
                                                });
                                addRule(
                                        "Debug Algae Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        algae,
                                        () -> algae.nudge(1));
                                addRule(
                                        "Debug Algae Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        algae,
                                        () -> algae.nudge(-1));
                                addRule(
                                        "Debug Algae Intake In",
                                        macropad.whenButton(InputConstants.INTAKE_IN),
                                        algae,
                                        () -> algae.setState(State.In));
                                addRule(
                                        "Debug Algae Intake Out",
                                        macropad.whenButton(InputConstants.INTAKE_OUT),
                                        algae,
                                        () -> algae.setState(State.Out));
                                addRule(
                                        "Debug Algae Stow",
                                        macropad.whenButton(InputConstants.STOW_POSITION),
                                        algae,
                                        () -> algae.setArmAngle(Level.Stow));
                                addRule(
                                        "Debug Algae Ground Intake",
                                        macropad.whenButton(InputConstants.GROUND_POSITION),
                                        algae,
                                        () -> algae.setArmAngle(Level.GroundIntake));
                                addRule(
                                        "Debug Algae Shoot Position",
                                        macropad.whenButton(InputConstants.SHOOT_POSITION),
                                        algae,
                                        () -> algae.setArmAngle(Level.Shoot));
                                addRule(
                                        "Debug Algae L2L3",
                                        macropad.whenButton(InputConstants.L2L3_POSITION),
                                        algae,
                                        () -> algae.setArmAngle(Level.L2L3AlgaeIntake));
                                addRule(
                                        "Debug Algae L3L4",
                                        macropad.whenButton(InputConstants.L3L4_POSITION),
                                        algae,
                                        () -> algae.setArmAngle(Level.L3L4AlgaeIntake));
                            }
                        });

        // simple one-button controls for intake
        // used for testing and tuning
        // allows for running intake at default intake/outtake speeds.
        addRule(
                "Debug Algae Shooter Feed",
                macropad.whenButton(InputConstants.ALGAE_SHOOTER_FEED),
                algae,
                () -> algae.setState(State.Feed));
        addRule(
                "Debug Algae Shooter On",
                macropad.whenButton(InputConstants.ALGAE_SHOOTER_ON),
                algae,
                () -> algae.setState(State.Shoot));

        // fine-grained control of the climber
        // used for testing and tuning
        // press down the climber control button and nudge the climber up and down
        addRule("Debug Control Elevator", macropad.whenButton(InputConstants.CONTROL_ELEVATOR))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                                "Debug Elevator Nudge No PID",
                                                macropad.whenButton(InputConstants.NUDGE_NO_PID))
                                        .whenTriggering(
                                                new RuleGroup() {
                                                    {
                                                        addRule(
                                                                        "Debug Elevator Nudge No PID Up",
                                                                        macropad.whenButton(
                                                                                InputConstants
                                                                                        .NUDGE_UP),
                                                                        elevator,
                                                                        () ->
                                                                                elevator.nudgeNoPID(
                                                                                        1.0))
                                                                .withFinishedTriggeringProcedure(
                                                                        elevator,
                                                                        () ->
                                                                                elevator.nudgeNoPID(
                                                                                        0));
                                                        addRule(
                                                                        "Debug Elevator Nudge No PID Down",
                                                                        macropad.whenButton(
                                                                                InputConstants
                                                                                        .NUDGE_DOWN),
                                                                        elevator,
                                                                        () ->
                                                                                elevator.nudgeNoPID(
                                                                                        -1.0))
                                                                .withFinishedTriggeringProcedure(
                                                                        elevator,
                                                                        () ->
                                                                                elevator.nudgeNoPID(
                                                                                        0));
                                                    }
                                                });
                                addRule(
                                        "Debug Elevator Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        elevator,
                                        () -> elevator.nudge(1));
                                addRule(
                                        "Debug Elevator Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        elevator,
                                        () -> elevator.nudge(-1));
                                addRule(
                                        "Debug Elevator Stow",
                                        macropad.whenButton(InputConstants.STOW_POSITION),
                                        elevator,
                                        () ->
                                                elevator.setPosition(
                                                        ElevatorPosition.ELEVATOR_BOTTOM));
                                addRule(
                                        "Debug Elevator Intake",
                                        macropad.whenButton(InputConstants.GROUND_POSITION),
                                        elevator,
                                        () ->
                                                elevator.setPosition(
                                                        ElevatorPosition.ELEVATOR_INTAKE));
                                addRule(
                                        "Debug Elevator Low",
                                        macropad.whenButton(InputConstants.SHOOT_POSITION),
                                        elevator,
                                        () -> elevator.setPosition(ElevatorPosition.ELEVATOR_L1));
                                addRule(
                                        "Debug Elevator Mid",
                                        macropad.whenButton(InputConstants.L2L3_POSITION),
                                        elevator,
                                        () -> elevator.setPosition(ElevatorPosition.ELEVATOR_L3));
                                addRule(
                                        "Debug Elevator High",
                                        macropad.whenButton(InputConstants.L3L4_POSITION),
                                        elevator,
                                        () -> elevator.setPosition(ElevatorPosition.ELEVATOR_L4));
                            }
                        });

        // fine-grained controls for shooter
        // used for testing and tuning
        // press down the intake control button and nudge ths shooter speed up and down
        addRule("Debug Control Wrist", macropad.whenButton(InputConstants.CONTROL_WRIST))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                                "Debug Wrist Nudge No PID",
                                                macropad.whenButton(InputConstants.NUDGE_NO_PID))
                                        .whenTriggering(
                                                new RuleGroup() {
                                                    {
                                                        addRule(
                                                                        "Debug Wrist Nudge No PID Up",
                                                                        macropad.whenButton(
                                                                                InputConstants
                                                                                        .NUDGE_UP),
                                                                        wrist,
                                                                        () -> wrist.nudgeNoPID(0.5))
                                                                .withFinishedTriggeringProcedure(
                                                                        wrist,
                                                                        () -> wrist.nudgeNoPID(0));
                                                        addRule(
                                                                        "Debug Wrist Nudge No PID Down",
                                                                        macropad.whenButton(
                                                                                InputConstants
                                                                                        .NUDGE_DOWN),
                                                                        wrist,
                                                                        () ->
                                                                                wrist.nudgeNoPID(
                                                                                        -0.5))
                                                                .withFinishedTriggeringProcedure(
                                                                        wrist,
                                                                        () -> wrist.nudgeNoPID(0));
                                                    }
                                                });
                                addRule(
                                        "Debug Wrist Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        wrist,
                                        () -> wrist.nudge(1));
                                addRule(
                                        "Debug Wrist Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        wrist,
                                        () -> wrist.nudge(-1));
                                addRule(
                                        "Debug Coral Graber In",
                                        macropad.whenButton(InputConstants.INTAKE_IN),
                                        coral,
                                        () -> coral.in()).withFinishedTriggeringProcedure(coral, () -> coral.stop());
                                addRule(
                                        "Debug Coral Grabber Out",
                                        macropad.whenButton(InputConstants.INTAKE_OUT),
                                        coral,
                                        () -> coral.out());
                                addRule(
                                        "Debug Wrist Stow",
                                        macropad.whenButton(InputConstants.STOW_POSITION),
                                        wrist,
                                        () -> wrist.setAngle(WristPosition.CORAL_BOTTOM));
                                addRule(
                                        "Debug Wrist Intake",
                                        macropad.whenButton(InputConstants.GROUND_POSITION),
                                        wrist,
                                        () -> wrist.setAngle(WristPosition.CORAL_INTAKE));
                                addRule(
                                        "Debug Wrist Low",
                                        macropad.whenButton(InputConstants.SHOOT_POSITION),
                                        wrist,
                                        () -> wrist.setAngle(WristPosition.CORAL_L1_PLACE));
                                addRule(
                                        "Debug Wrist Mid",
                                        macropad.whenButton(InputConstants.L2L3_POSITION),
                                        wrist,
                                        () -> wrist.setAngle(WristPosition.CORAL_L2_PLACE));
                                addRule(
                                        "Debug Wrist High",
                                        macropad.whenButton(InputConstants.L3L4_POSITION),
                                        wrist,
                                        () -> wrist.setAngle(WristPosition.CORAL_L4_PLACE));
                            }
                        });

        addRule("Debug Control Climber", macropad.whenButton(InputConstants.CONTROL_CLIMBER))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Debug Climber Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        climber,
                                        () -> climber.climb(0.5));
                                addRule(
                                        "Debug Climber Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        climber,
                                        () -> climber.climb(-0.5));
                            }
                        });
    }
}

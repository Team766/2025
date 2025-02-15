package com.team766.robot.reva_2025.OI;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;
import com.team766.robot.reva_2025.mechanisms.Climber;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;

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
 * 1 + 8/12 = Control Shoulder + Nudge Up/Down
 * 2 + 8/12 = Control Climber + Nudge Up/Down
 * 4 + 8/12 = Control Shooter + Nudge Up/Down
 *  3        = Intake In
 * 7        = Intake Out
 */
public class DebugOI extends RuleGroup {
    public DebugOI(
            JoystickReader macropad,
            Climber climber,
            Elevator elevator,
            Wrist wrist,
            AlgaeIntake algae,
            CoralIntake coral) {
        // fine-grained control of the shoulder
        // used for testing and tuning
        // press down the shoulder control button and nudge the angle up and down
        addRule("Debug Control Algae", macropad.whenButton(1))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule("Debug Algae Nudge No PID", macropad.whenButton(7))
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
                                                                        algae, () -> algae.nudgeNoPID(0));
                                                        addRule(
                                                                "Debug Algae Nudge No PID Down",
                                                                macropad.whenButton(
                                                                        InputConstants.NUDGE_DOWN),
                                                                algae,
                                                                () -> algae.nudgeNoPID(-0.2))
                                                                .withFinishedTriggeringProcedure(
                                                                        algae, () -> algae.nudgeNoPID(0));
                                                    }
                                                });
                                addRule(
                                        "Debug Algae Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        algae,
                                        () -> algae.nudgeUp());
                                addRule(
                                        "Debug Algae Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        algae,
                                        () -> algae.nudgeDown());
                                addRule("Debug Algae Intake In", macropad.whenButton(5), algae, () -> algae.in());
                                addRule("Debug Algae Intake Out", macropad.whenButton(6), algae, () -> algae.out());
                                addRule(
                                        "Debug Algae Stow",
                                        macropad.whenButton(9),
                                        algae,
                                        () -> algae.setArmAngle(Level.Stow));
                                addRule(
                                        "Debug Algae Ground Intake",
                                        macropad.whenButton(13),
                                        algae,
                                        () -> algae.setArmAngle(Level.GroundIntake));
                                addRule(
                                        "Debug Algae Shoot Position",
                                        macropad.whenButton(14),
                                        algae,
                                        () -> algae.setArmAngle(Level.Shoot));
                                addRule(
                                        "Debug Algae L2L3",
                                        macropad.whenButton(15),
                                        algae,
                                        () -> algae.setArmAngle(Level.L2L3AlgaeIntake));
                                addRule(
                                        "Debug Algae L3L4",
                                        macropad.whenButton(16),
                                        algae,
                                        () -> algae.setArmAngle(Level.L3L4AlgaeIntake));
                                }
                        });

        // simple one-button controls for intake
        // used for testing and tuning
        // allows for running intake at default intake/outtake speeds.
        addRule("Debug Algae Shooter Feed", macropad.whenButton(11), algae, () -> algae.feed());
        addRule("Debug Algae Shooter On", macropad.whenButton(10), algae, () -> algae.shooterOn());


        // fine-grained control of the climber
        // used for testing and tuning
        // press down the climber control button and nudge the climber up and down
        addRule("Debug Control Elevator", macropad.whenButton(2))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule("Debug Elevator Nudge No PID", macropad.whenButton(7))
                                        .whenTriggering(
                                                new RuleGroup() {
                                                    {
                                                        addRule(
                                                                        "Debug Elevator Nudge No PID Up",
                                                                        macropad.whenButton(
                                                                                InputConstants
                                                                                        .NUDGE_UP),
                                                                        elevator,
                                                                        () -> elevator.nudgeNoPID(1.0))
                                                                .withFinishedTriggeringProcedure(
                                                                        elevator, () -> elevator.nudgeNoPID(0));
                                                        addRule(
                                                                "Debug Elevator Nudge No PID Down",
                                                                macropad.whenButton(
                                                                        InputConstants.NUDGE_DOWN),
                                                                elevator,
                                                                () -> elevator.nudgeNoPID(-1.0))
                                                                .withFinishedTriggeringProcedure(
                                                                        elevator, () -> elevator.nudgeNoPID(0));
                                                    }
                                                });
                                addRule(
                                        "Debug Elevator Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        elevator,
                                        () -> elevator.nudgeUp());
                                addRule(
                                        "Debug Elevator Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        elevator,
                                        () -> elevator.nudgeDown());
                                addRule(
                                        "Debug Elevator Stow",
                                        macropad.whenButton(9),
                                        elevator,
                                        () -> elevator.setPosition(ElevatorPosition.ELEVATOR_BOTTOM));
                                addRule(
                                        "Debug Elevator Intake",
                                        macropad.whenButton(13),
                                        elevator,
                                        () -> elevator.setPosition(ElevatorPosition.ELEVATOR_INTAKE));
                                addRule(
                                        "Debug Elevator Low",
                                        macropad.whenButton(15),
                                        elevator,
                                        () -> elevator.setPosition(ElevatorPosition.ELEVATOR_LOW));
                                addRule(
                                        "Debug Elevator High",
                                        macropad.whenButton(16),
                                        elevator,
                                        () -> elevator.setPosition(ElevatorPosition.ELEVATOR_HIGH));
                            }
                        });

        // fine-grained controls for shooter
        // used for testing and tuning
        // press down the intake control button and nudge ths shooter speed up and down
        addRule("Debug Control Wrist", macropad.whenButton(3))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule("Debug Wrist Nudge No PID", macropad.whenButton(7))
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
                                                                        wrist, () -> wrist.nudgeNoPID(0));
                                                        addRule(
                                                                "Debug Wrist Nudge No PID Down",
                                                                macropad.whenButton(
                                                                        InputConstants.NUDGE_DOWN),
                                                                wrist,
                                                                () -> wrist.nudgeNoPID(-0.5))
                                                                .withFinishedTriggeringProcedure(
                                                                        wrist, () -> wrist.nudgeNoPID(0));
                                                    }
                                                });
                                addRule(
                                        "Debug Wrist Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        wrist,
                                        () -> wrist.nudgeUp());
                                addRule(
                                        "Debug Wrist Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        wrist,
                                        () -> wrist.nudgeDown());
                                addRule("Debug Coral Graber In", macropad.whenButton(5), coral, () -> coral.in());
                                addRule("Debug Coral Grabber Out", macropad.whenButton(6), coral, () -> coral.out());
                                addRule(
                                        "Debug Wrist Stow",
                                        macropad.whenButton(9),
                                        wrist,
                                        () -> wrist.setPosition(WristPosition.WRIST_BOTTOM));
                                addRule(
                                        "Debug Wrist Intake",
                                        macropad.whenButton(13),
                                        wrist,
                                        () -> wrist.setPosition(WristPosition.WRIST_INTAKE));
                                addRule(
                                        "Debug Wrist Low",
                                        macropad.whenButton(14),
                                        wrist,
                                        () -> wrist.setPosition(WristPosition.WRIST_LOW));
                                addRule(
                                        "Debug Wrist Mid",
                                        macropad.whenButton(15),
                                        wrist,
                                        () -> wrist.setPosition(WristPosition.WRIST_MID));
                                addRule(
                                        "Debug Wrist High",
                                        macropad.whenButton(16),
                                        wrist,
                                        () -> wrist.setPosition(WristPosition.WRIST_HIGH));
                        }
                }
        );

        addRule("Debug Control Climber", macropad.whenButton(4))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Debug Climber Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        climber,
                                        () -> climber.climberUp());
                                addRule(
                                        "Debug Climber Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        climber,
                                        () -> climber.climberDown());
                            }
                        });
    }
}

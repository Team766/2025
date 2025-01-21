package com.team766.robot.reva;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva.constants.InputConstants;
import com.team766.robot.reva.mechanisms.Climber;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import com.team766.robot.reva.mechanisms.Shoulder;

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
            Shoulder shoulder,
            Intake intake,
            Shooter shooter) {
        // fine-grained control of the shoulder
        // used for testing and tuning
        // press down the shoulder control button and nudge the angle up and down
        addRule("Debug Control Shoulder", macropad.whenButton(InputConstants.CONTROL_SHOULDER))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Debug Shoulder Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        shoulder,
                                        () -> shoulder.nudgeUp());
                                addRule(
                                        "Debug Shoulder Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        shoulder,
                                        () -> shoulder.nudgeDown());
                                addRule(
                                        "Debug Shoulder Reset",
                                        macropad.whenButton(InputConstants.MACROPAD_RESET_SHOULDER),
                                        shoulder,
                                        () -> shoulder.reset());
                            }
                        });

        addRule(
                "Debug Climber Reset",
                macropad.whenButton(16),
                climber,
                () -> {
                    climber.resetLeftPosition();
                    climber.resetRightPosition();
                });

        // fine-grained control of the climber
        // used for testing and tuning
        // press down the climber control button and nudge the climber up and down
        addRule("Debug Control Climber", macropad.whenButton(InputConstants.CONTROL_CLIMBER))
                .withOnTriggeringProcedure(
                        ONCE,
                        climber,
                        () -> {
                            climber.enableSoftLimits(false);
                        })
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Debug Climber Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        climber,
                                        () -> climber.setPower(-0.25));
                                addRule(
                                        "Debug Climber Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        climber,
                                        () -> climber.setPower(0.25));
                            }
                        })
                .withFinishedTriggeringProcedure(
                        climber,
                        () -> {
                            climber.stop();
                            climber.enableSoftLimits(true);
                        });

        // simple one-button controls for intake
        // used for testing and tuning
        // allows for running intake at default intake/outtake speeds.
        addRule(
                "Debug Intake In",
                macropad.whenButton(InputConstants.INTAKE_IN),
                intake,
                () -> intake.in());
        addRule(
                "Debug Intake Out",
                macropad.whenButton(InputConstants.INTAKE_OUT),
                intake,
                () -> intake.out());

        // fine-grained controls for shooter
        // used for testing and tuning
        // press down the intake control button and nudge ths shooter speed up and down
        addRule("Debug Control Shooter", macropad.whenButton(InputConstants.CONTROL_SHOOTER))
                .withOnTriggeringProcedure(ONCE, shooter, () -> shooter.shoot())
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Debug Shooter Nudge Up",
                                        macropad.whenButton(InputConstants.NUDGE_UP),
                                        shooter,
                                        () -> shooter.nudgeUp());
                                addRule(
                                        "Debug Shooter Nudge Down",
                                        macropad.whenButton(InputConstants.NUDGE_DOWN),
                                        shooter,
                                        () -> shooter.nudgeDown());
                            }
                        })
                .withFinishedTriggeringProcedure(shooter, () -> shooter.stop());
    }
}

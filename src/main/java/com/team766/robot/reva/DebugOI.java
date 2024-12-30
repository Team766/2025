package com.team766.robot.reva;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.Rule;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva.constants.InputConstants;
import com.team766.robot.reva.mechanisms.ArmAndClimber;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import java.util.Set;

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
    public DebugOI(JoystickReader macropad, ArmAndClimber ss, Intake intake, Shooter shooter) {
        // fine-grained control of the shoulder
        // used for testing and tuning
        // press down the shoulder control button and nudge the angle up and down
        addRule(
                Rule.create(
                                "Debug Control Shoulder",
                                () -> macropad.getButton(InputConstants.CONTROL_SHOULDER))
                        .whenTriggering(
                                Rule.create(
                                                "Debug Shoulder Nudge Up",
                                                () -> macropad.getButton(InputConstants.NUDGE_UP))
                                        .onTriggering(
                                                ONCE,
                                                Set.of(ss),
                                                () -> ss.requestShoulderNudgeUp()),
                                Rule.create(
                                                "Debug Shoulder Nudge Down",
                                                () -> macropad.getButton(InputConstants.NUDGE_DOWN))
                                        .onTriggering(
                                                ONCE,
                                                Set.of(ss),
                                                () -> ss.requestShoulderNudgeDown()),
                                Rule.create(
                                                "Debug Shoulder Reset",
                                                () ->
                                                        macropad.getButton(
                                                                InputConstants
                                                                        .MACROPAD_RESET_SHOULDER))
                                        .onTriggering(ONCE, Set.of(ss), () -> ss.resetShoulder())));

        addRule(
                Rule.create("Debug Climber Reset", () -> macropad.getButton(16))
                        .onTriggering(ONCE, Set.of(ss), () -> ss.resetClimberPositions()));

        // fine-grained control of the climber
        // used for testing and tuning
        // press down the climber control button and nudge the climber up and down
        addRule(
                Rule.create(
                                "Debug Control Climber",
                                () -> macropad.getButton(InputConstants.CONTROL_CLIMBER))
                        .whenTriggering(
                                Rule.create(
                                                "Debug Climber Nudge Up",
                                                () -> macropad.getButton(InputConstants.NUDGE_UP))
                                        .onTriggering(
                                                ONCE,
                                                Set.of(ss),
                                                () ->
                                                        ss.requestClimberMotorPowers(
                                                                -0.25, -0.25, true)),
                                Rule.create(
                                                "Debug Climber Nudge Down",
                                                () -> macropad.getButton(InputConstants.NUDGE_DOWN))
                                        .onTriggering(
                                                ONCE,
                                                Set.of(ss),
                                                () ->
                                                        ss.requestClimberMotorPowers(
                                                                0.25, 0.25, true)))
                        .onFinishedTriggering(Set.of(ss), () -> ss.requestClimberStop()));

        // simple one-button controls for intake
        // used for testing and tuning
        // allows for running intake at default intake/outtake speeds.
        addRule(
                Rule.create("Debug Intake In", () -> macropad.getButton(InputConstants.INTAKE_IN))
                        .onTriggering(ONCE_AND_HOLD, Set.of(intake), () -> intake.requestIn()));
        addRule(
                Rule.create("Debug Intake Out", () -> macropad.getButton(InputConstants.INTAKE_OUT))
                        .onTriggering(ONCE_AND_HOLD, Set.of(intake), () -> intake.requestOut()));

        // fine-grained controls for shooter
        // used for testing and tuning
        // press down the intake control button and nudge ths shooter speed up and down
        addRule(
                Rule.create(
                                "Debug Control Shooter",
                                () -> macropad.getButton(InputConstants.CONTROL_SHOOTER))
                        .onTriggering(
                                ONCE_AND_HOLD, Set.of(shooter), () -> shooter.requestResumeShoot())
                        .whenTriggering(
                                Rule.create(
                                                "Debug Shooter Nudge Up",
                                                () -> macropad.getButton(InputConstants.NUDGE_UP))
                                        .onTriggering(
                                                ONCE,
                                                Set.of(shooter),
                                                () -> shooter.requestNudgeUp()),
                                Rule.create(
                                                "Debug Shooter Nudge Down",
                                                () -> macropad.getButton(InputConstants.NUDGE_DOWN))
                                        .onTriggering(
                                                ONCE,
                                                Set.of(shooter),
                                                () -> shooter.requestNudgeDown()))
                        .onFinishedTriggering(Set.of(shooter), () -> shooter.requestStop()));
    }
}

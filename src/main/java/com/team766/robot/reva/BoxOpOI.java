package com.team766.robot.reva;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.reva.constants.InputConstants;
import com.team766.robot.reva.mechanisms.Climber;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import com.team766.robot.reva.mechanisms.Shoulder;
import com.team766.robot.reva.procedures.IntakeUntilIn;

public class BoxOpOI extends RuleGroup {
    public BoxOpOI(
            JoystickReader gamepad,
            Climber climber,
            Shoulder shoulder,
            Intake intake,
            Shooter shooter) {
        gamepad.setAxisDeadzone(InputConstants.XBOX_LS_Y, ControlConstants.JOYSTICK_DEADZONE);
        gamepad.setAxisDeadzone(InputConstants.XBOX_RS_Y, ControlConstants.JOYSTICK_DEADZONE);

        // climber

        addRule(
                        "Climber Mode",
                        gamepad.whenAllButtons(InputConstants.XBOX_A, InputConstants.XBOX_B))
                .withOnTriggeringProcedure(
                        // move the shoulder out of the way
                        ONCE, shoulder, () -> shoulder.rotate(Shoulder.ShoulderPosition.TOP))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                // if the sticks are being moving, move the corresponding climber(s)
                                addRule(
                                                "Move climbers",
                                                gamepad.whenAnyAxisMoved(
                                                        InputConstants.XBOX_LS_Y,
                                                        InputConstants.XBOX_RS_Y),
                                                REPEATEDLY,
                                                climber,
                                                () -> {
                                                    boolean overrideSoftLimits =
                                                            gamepad.getButton(InputConstants.XBOX_X)
                                                                    && gamepad.getButton(
                                                                            InputConstants.XBOX_Y);
                                                    climber.enableSoftLimits(!overrideSoftLimits);
                                                    climber.setLeftPower(
                                                            gamepad.getAxis(
                                                                    InputConstants.XBOX_LS_Y));
                                                    climber.setRightPower(
                                                            gamepad.getAxis(
                                                                    InputConstants.XBOX_RS_Y));
                                                })
                                        .withFinishedTriggeringProcedure(
                                                climber, () -> climber.stop());
                            }
                        })
                .withFinishedTriggeringProcedure(
                        // restore the shoulder (and stop the climber)
                        shoulder, () -> shoulder.rotate(85))
                .whenNotTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Shoulder to Intake",
                                        gamepad.whenButton(InputConstants.XBOX_A),
                                        shoulder,
                                        () ->
                                                shoulder.rotate(
                                                        Shoulder.ShoulderPosition.INTAKE_FLOOR));
                                addRule(
                                        "Shoulder to close shot",
                                        gamepad.whenButton(InputConstants.XBOX_B),
                                        shoulder,
                                        () -> shoulder.rotate(Shoulder.ShoulderPosition.SHOOT_LOW));
                                addRule(
                                        "Shoulder to amp shot",
                                        gamepad.whenButton(InputConstants.XBOX_X),
                                        shoulder,
                                        () -> shoulder.rotate(Shoulder.ShoulderPosition.AMP));
                                addRule(
                                                "Shoulder to assist shot",
                                                gamepad.whenButton(InputConstants.XBOX_Y),
                                                shoulder,
                                                () ->
                                                        shoulder.rotate(
                                                                Shoulder.ShoulderPosition
                                                                        .SHOOTER_ASSIST))
                                        .whenTriggering(
                                                new RuleGroup() {
                                                    {
                                                        addRule(
                                                                "Spin shooter for assist shot",
                                                                whenStatusMatching(
                                                                        Shooter.ShooterStatus.class,
                                                                        s ->
                                                                                s.targetSpeed()
                                                                                        != 0.0),
                                                                shooter,
                                                                () ->
                                                                        shooter.shoot(
                                                                                Shooter
                                                                                        .SHOOTER_ASSIST_SPEED));
                                                    }
                                                });
                                addRule(
                                        "Nudge Shoulder Up",
                                        () -> gamepad.getPOV() == 0,
                                        shoulder,
                                        () -> shoulder.nudgeUp());
                                addRule(
                                        "Nudge Shoulder Down",
                                        () -> gamepad.getPOV() == 180,
                                        shoulder,
                                        () -> shoulder.nudgeDown());
                            }
                        });

        // shooter
        addRule(
                "Spin Shooter",
                gamepad.whenAxisMoved(InputConstants.XBOX_RT),
                shooter,
                () -> shooter.shoot(4800));

        // intake
        addRule(
                "Intake Out",
                gamepad.whenButton(InputConstants.XBOX_RB),
                intake,
                () -> intake.out());
        addRule(
                "Intake Until In",
                gamepad.whenButton(InputConstants.XBOX_LB),
                () -> new IntakeUntilIn(intake));

        // // rumble
        // // TODO(MF3): Add the ability to reserve joysticks
        // addRule(
        //         "Rumble when holding note",
        //         () -> checkForStatusWith(Intake.IntakeStatus.class, s -> s.hasNoteInIntake()))
        //         .withOnTriggeringProcedure(ONCE_AND_HOLD, Set.of(), () -> ((GenericHID) gamepad)
        //                 .setRumble(RumbleType.kBothRumble, 0.5))
        //         .withFinishedTriggeringProcedure(Set.of(), () -> ((GenericHID) gamepad)
        //                 .setRumble(RumbleType.kBothRumble, 0.0));
    }
}

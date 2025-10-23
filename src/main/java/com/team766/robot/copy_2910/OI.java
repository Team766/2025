package com.team766.robot.copy_2910;

import static com.team766.framework.RulePersistence.ONCE;
import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;

import com.team766.framework.Conditions;
import com.team766.framework.RuleEngine;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.copy_2910.mechanisms.Elevator;
import com.team766.robot.copy_2910.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.copy_2910.mechanisms.Intake;
import com.team766.robot.copy_2910.mechanisms.Shoulder;
import com.team766.robot.copy_2910.mechanisms.Shoulder.ShoulderPosition;
import com.team766.robot.copy_2910.mechanisms.Vision;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import com.team766.robot.copy_2910.mechanisms.Wrist.WristPosition;
import com.team766.robot.copy_2910.procedures.IntakeCoral;
import com.team766.robot.copy_2910.procedures.IntakeCoralL1;
import com.team766.robot.copy_2910.procedures.MoveWristvator;
import com.team766.robot.copy_2910.procedures.OuttakeCoral;
import com.team766.robot.copy_2910.procedures.ShootAlgae;
import java.util.Set;

public class OI extends RuleEngine {

    public static class QueuedControl {
        public Wrist.WristPosition wristPosition;
        public Shoulder.ShoulderPosition shoulderPosition;
        public Elevator.ElevatorPosition elevatorPosition;
    }

    public OI(
            SwerveDrive swerveDrive,
            Intake intake,
            Wrist wrist,
            Elevator elevator,
            Shoulder shoulder,
            Vision vision) {

        // final JoystickReader leftJoystick = RobotProvider.instance.getJoystick(0);
        // final JoystickReader rightJoystick = RobotProvider.instance.getJoystick(1);
        final JoystickReader boxopGamepad = RobotProvider.instance.getJoystick(0); // previously 2
        // leftJoystick.setAllAxisDeadzone(0.05);
        // rightJoystick.setAllAxisDeadzone(0.05);
        boxopGamepad.setAllAxisDeadzone(0.05);

        QueuedControl queuedControl = new QueuedControl();
        queuedControl.wristPosition = WristPosition.STOW;
        queuedControl.shoulderPosition = ShoulderPosition.STOW;
        queuedControl.elevatorPosition = ElevatorPosition.READY;

        addRules(new DriverOI(boxopGamepad, swerveDrive));
        // addRules(
        //        new BoxOpOI(
        //                boxopGamepad, shoulder, elevator, wrist, climber, intake, queuedControl));

        /*addRule(
                        "Outtake Algae",
                        boxopGamepad.whenButton(InputConstants.TEMP), //TODO: algaeMode? variable
                        ONCE_AND_HOLD,
                        intake,
                        () -> intake.setAlgaePower(-0.5))
                .withFinishedTriggeringProcedure(intake, () -> intake.stop());
        */
        addRule(
                "Wrist Nudge Up",
                // TODO: () -> syntax correctness
                () -> boxopGamepad.getPOV() == InputConstants.GAMEPAD_DPAD_RIGHT,
                ONCE,
                wrist,
                () -> wrist.nudgeUp());
        addRule(
                "Wrist Nudge Down",
                () -> boxopGamepad.getPOV() == InputConstants.GAMEPAD_DPAD_LEFT,
                ONCE,
                wrist,
                () -> wrist.nudgeDown());
        /*
        addRule("Elevator Nudge Up",
                rightJoystick.whenButton(2),
                ONCE,
                elevator,
                () -> elevator.nudgeUp());
        addRule(
                "Elevator Nudge Down",
                rightJoystick.whenButton(3),
                ONCE,
                elevator,
                () -> elevator.nudgeDown());
        */
        // CLIMBER

        // addRule(
        //                 "Enable Climber",
        //                 boxopGamepad.whenButton(InputConstants.GAMEPAD_BACK_BUTTON),
        //                 ONCE,
        //                 Set.of(climber, wrist, elevator, shoulder),
        //                 () -> {
        //                     elevator.setPosition(
        //                             Elevator.ElevatorPosition.STOW); // previously MAXIMUM
        //                     wrist.setPosition(Wrist.WristPosition.ALGAE_LOW);
        //                     climber.setClimberSpeed(0.5);
        //                     shoulder.setPosition(Shoulder.ShoulderPosition.CLIMBER);
        //                 }) // TODO: make sure whenTriggering is the right rule & not going to
        // break
        //         // anything
        //         .whenTriggering(
        //                 new RuleGroup() {
        //                     {
        //                         addRule(
        //                                 "Move Climber",
        //
        // boxopGamepad.whenButton(InputConstants.GAMEPAD_DPAD_DOWN),
        //                                 ONCE,
        //                                 Set.of(climber, wrist, elevator, shoulder),
        //                                 () -> {
        //                                     climber.stop();
        //                                     shoulder.setPosition(ShoulderPosition.CORAL_GROUND);
        //                                 });
        //                     }
        //                 });

        addRule(
                        "Toggle Coral or Algae Mode",
                        new Conditions.Toggle(() -> boxopGamepad.getPOV() == 0))
                .withOnTriggeringProcedure(ONCE, Set.of(intake), () -> intake.stopAlgae())
                .withFinishedTriggeringProcedure(Set.of(intake), () -> intake.stopAlgae())
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Move Algae Intake to Low Position",
                                        boxopGamepad.whenButton(InputConstants.GAMEPAD_A_BUTTON),
                                        ONCE,
                                        Set.of(elevator, shoulder, intake, wrist),
                                        (context) -> {
                                            queuedControl.elevatorPosition =
                                                    ElevatorPosition.ALGAE_LOW;
                                            queuedControl.shoulderPosition =
                                                    ShoulderPosition.ALGAE_LOW;
                                            queuedControl.wristPosition = WristPosition.ALGAE_LOW;
                                            context.runSync(
                                                    new MoveWristvator(
                                                            shoulder,
                                                            elevator,
                                                            wrist,
                                                            queuedControl.shoulderPosition,
                                                            queuedControl.elevatorPosition,
                                                            queuedControl.wristPosition));
                                        });

                                addRule(
                                        "Move Algae Intake to High Position",
                                        boxopGamepad.whenButton(InputConstants.GAMEPAD_Y_BUTTON),
                                        ONCE,
                                        Set.of(elevator, shoulder, intake, wrist),
                                        (context) -> {
                                            queuedControl.elevatorPosition =
                                                    ElevatorPosition.ALGAE_HIGH;
                                            queuedControl.shoulderPosition =
                                                    ShoulderPosition.ALGAE_HIGH;
                                            queuedControl.wristPosition = WristPosition.ALGAE_HIGH;
                                            context.runSync(
                                                    new MoveWristvator(
                                                            shoulder,
                                                            elevator,
                                                            wrist,
                                                            queuedControl.shoulderPosition,
                                                            queuedControl.elevatorPosition,
                                                            queuedControl.wristPosition));
                                        });

                                addRule(
                                                "Intake Algae",
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants.GAMEPAD_LEFT_TRIGGER),
                                                ONCE_AND_HOLD,
                                                Set.of(intake),
                                                () -> intake.turnAlgaeNegative())
                                        .withFinishedTriggeringProcedure(
                                                intake, () -> intake.retainAlgae());

                                addRule(
                                                "Shoot Algae",
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants.GAMEPAD_RIGHT_TRIGGER),
                                                ONCE_AND_HOLD,
                                                Set.of(elevator, shoulder, intake, wrist),
                                                (context) -> {
                                                    queuedControl.elevatorPosition =
                                                            ElevatorPosition.ALGAE_SHOOT;
                                                    queuedControl.shoulderPosition =
                                                            ShoulderPosition.ALGAE_HIGH;
                                                    queuedControl.wristPosition =
                                                            WristPosition.ALGAE_SHOOT;
                                                    context.runParallel(
                                                            new MoveWristvator(
                                                                    shoulder,
                                                                    elevator,
                                                                    wrist,
                                                                    queuedControl.shoulderPosition,
                                                                    queuedControl.elevatorPosition,
                                                                    queuedControl.wristPosition),
                                                            new ShootAlgae(intake));
                                                })
                                        .withFinishedTriggeringProcedure(
                                                intake, () -> intake.stopAlgae());
                            }
                        })
                .whenNotTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                        "Move Elevator and Wrist to L1 Position",
                                        boxopGamepad.whenButton(InputConstants.GAMEPAD_A_BUTTON),
                                        ONCE,
                                        () -> {
                                            queuedControl.elevatorPosition = ElevatorPosition.L1;
                                            queuedControl.shoulderPosition = ShoulderPosition.L1;
                                            queuedControl.wristPosition = WristPosition.L1;
                                            return new MoveWristvator(
                                                    shoulder,
                                                    elevator,
                                                    wrist,
                                                    queuedControl.shoulderPosition,
                                                    queuedControl.elevatorPosition,
                                                    queuedControl.wristPosition);
                                        });

                                addRule(
                                        "Move Elevator and Wrist to L2 Position",
                                        boxopGamepad.whenButton(InputConstants.GAMEPAD_B_BUTTON),
                                        ONCE,
                                        () -> {
                                            queuedControl.elevatorPosition = ElevatorPosition.L2;
                                            queuedControl.shoulderPosition = ShoulderPosition.L2;
                                            queuedControl.wristPosition = WristPosition.L2;
                                            return new MoveWristvator(
                                                    shoulder,
                                                    elevator,
                                                    wrist,
                                                    queuedControl.shoulderPosition,
                                                    queuedControl.elevatorPosition,
                                                    queuedControl.wristPosition);
                                        });

                                addRule(
                                        "Move Elevator and Wrist to L3 Position",
                                        boxopGamepad.whenButton(InputConstants.GAMEPAD_X_BUTTON),
                                        ONCE,
                                        () -> {
                                            queuedControl.elevatorPosition = ElevatorPosition.L3;
                                            queuedControl.shoulderPosition = ShoulderPosition.L3;
                                            queuedControl.wristPosition = WristPosition.L3;
                                            return new MoveWristvator(
                                                    shoulder,
                                                    elevator,
                                                    wrist,
                                                    queuedControl.shoulderPosition,
                                                    queuedControl.elevatorPosition,
                                                    queuedControl.wristPosition);
                                        });

                                addRule(
                                        "Move Elevator and Wrist to L4 Position",
                                        boxopGamepad.whenButton(InputConstants.GAMEPAD_Y_BUTTON),
                                        ONCE,
                                        () -> {
                                            queuedControl.elevatorPosition = ElevatorPosition.L4;
                                            queuedControl.shoulderPosition = ShoulderPosition.L4;
                                            queuedControl.wristPosition = WristPosition.L4;
                                            return new MoveWristvator(
                                                    shoulder,
                                                    elevator,
                                                    wrist,
                                                    queuedControl.shoulderPosition,
                                                    queuedControl.elevatorPosition,
                                                    queuedControl.wristPosition);
                                        });

                                addRule(
                                                "Outtake Coral",
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants.GAMEPAD_RIGHT_TRIGGER),
                                                ONCE_AND_HOLD,
                                                () -> new OuttakeCoral(intake))
                                        .withFinishedTriggeringProcedure(
                                                intake, () -> intake.stop());
                                addRule(
                                                "Ground Intake",
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants.GAMEPAD_LEFT_TRIGGER),
                                                ONCE_AND_HOLD,
                                                () -> {
                                                    return new IntakeCoral(
                                                            intake, elevator, shoulder, wrist);
                                                })
                                        .withFinishedTriggeringProcedure(
                                                Set.of(intake, elevator, wrist, shoulder),
                                                () -> {
                                                    intake.stop();
                                                    elevator.setPosition(ElevatorPosition.READY);
                                                    wrist.setPosition(WristPosition.STOW);
                                                    shoulder.setPosition(ShoulderPosition.STOW);
                                                });
                                addRule(
                                                "L1 Ground Intake",
                                                boxopGamepad.whenButton(
                                                        InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                                                ONCE_AND_HOLD,
                                                () ->
                                                        new IntakeCoralL1(
                                                                intake, elevator, shoulder, wrist))
                                        .withFinishedTriggeringProcedure(
                                                Set.of(intake, elevator, wrist, shoulder),
                                                () -> {
                                                    intake.stop();
                                                    elevator.setPosition(ElevatorPosition.READY);
                                                    wrist.setPosition(WristPosition.STOW);
                                                    shoulder.setPosition(ShoulderPosition.STOW);
                                                });
                            }
                        });

        // ALGAE INTAKE POSITIONS

        // Unnecessary for Subjorn?
        // addRule(
        //          "Queue to Algae Ground Intake Position",
        //          () -> boxopGamepad.getPOV() == InputConstants.BUTTON_ALGAE_INTAKE_GROUND,
        //          ONCE,
        //          Set.of(elevator, shoulder, intake),
        //          () -> {
        //              queuedControl.elevatorPosition = ElevatorPosition.ALGAE_GROUND;
        //              queuedControl.shoulderPosition = ShoulderPosition.ALGAE_GROUND;
        //              queuedControl.wristPosition = WristPosition.ALGAE_GROUND;
        //          });

        // TODO: check if Spin Algae Motor In is necessary
        /*addRule(
                        "Move to Target Position",
                        boxopGamepad.whenButton(InputConstants.GAMEPAD_RIGHT_BUMPER_BUTTON),
                        ONCE_AND_HOLD,
                        () ->
                                new MoveWristvator(
                                        shoulder,
                                        elevator,
                                        wrist,
                                        queuedControl.shoulderPosition,
                                        queuedControl.elevatorPosition,
                                        queuedControl.wristPosition))
                .whenTriggering(
                        new RuleGroup() {
                            {
                                addRule(
                                                "Spin Algae Intake Motor In",
                                                boxopGamepad.whenAxisMoved(
                                                        InputConstants
                                                                .BUTTON_ALGAE_MOTOR_INTAKE_POWER),
                                                ONCE_AND_HOLD,
                                                Set.of(intake),
                                                context -> {
                                                    intake.setAlgaePower(0.5);
                                                })
                                        .withFinishedTriggeringProcedure(
                                                intake, () -> intake.setAlgaePower(0.1));
                                //TODO: check if nudge shoulder below is necessary

                                addRule(
                                        "Nudge Shoulder",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.AXIS_WRIST_FINETUNE),
                                        ONCE_AND_HOLD,
                                        shoulder,
                                        () -> {
                                            shoulder.nudge(
                                                    boxopGamepad.getAxis(
                                                            InputConstants.AXIS_WRIST_FINETUNE));
                                        });
                            }
                        });
        */

        /*
        addRule("Nudge Shoulder Up",
                boxopGamepad.whenButton(9),
                ONCE,
                shoulder,
                () -> shoulder.nudgeUp());
        addRule("Nudge Shoulder Down",
                boxopGamepad.whenButton(10),
                ONCE,
                shoulder,
                () -> shoulder.nudgeDown());
        */

        // TODO: Set POV 0 to Algae Mode toggle

        /*addRule("Stow",
        () -> boxopGamepad.getPOV() == 0,
        ONCE,
        Set.of(elevator, shoulder, intake),
                () -> {
                    queuedControl.elevatorPosition = ElevatorPosition.STOW;
                    queuedControl.shoulderPosition = ShoulderPosition.STOW;
                    queuedControl.wristPosition = WristPosition.STOW;
                }); */

        // addRule(
        //         "Apply queued positions",
        //         leftJoystick.whenButton(4),
        //         ONCE_AND_HOLD,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             wrist.setSetpoint(queuedControl.wristPosition.getPosition());
        //             shoulder.setSetpoint(queuedControl.shoulderPosition.getPosition());
        //             elevator.setPosition(queuedControl.elevatorPosition.getPosition());
        //         });

        // addRule(
        //         "Prep L1 Coral",
        //         leftJoystick.whenButton(5),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.L1;
        //             queuedControl.shoulderPosition = ShoulderPosition.L1;
        //             queuedControl.elevatorPosition = ElevatorPosition.L1;
        //         });
        // addRule(
        //         "Prep L2 Coral",
        //         leftJoystick.whenButton(6),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.L2;
        //             queuedControl.shoulderPosition = ShoulderPosition.L2;
        //             queuedControl.elevatorPosition = ElevatorPosition.L2;
        //         });
        // addRule(
        //         "Prep L3 Coral",
        //         leftJoystick.whenButton(7),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.L3;
        //             queuedControl.shoulderPosition = ShoulderPosition.L3;
        //             queuedControl.elevatorPosition = ElevatorPosition.L3;
        //         });
        // addRule(
        //         "Prep L4 Coral",
        //         leftJoystick.whenButton(8),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.L4;
        //             queuedControl.shoulderPosition = ShoulderPosition.L4;
        //             queuedControl.elevatorPosition = ElevatorPosition.L4;
        //         });
        // addRule(
        //         "Prep Algae High",
        //         leftJoystick.whenButton(9),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.ALGAE_HIGH;
        //             queuedControl.shoulderPosition = ShoulderPosition.ALGAE_HIGH;
        //             queuedControl.elevatorPosition = ElevatorPosition.ALGAE_HIGH;
        //         });
        // addRule(
        //         "Prep Algae Low",
        //         leftJoystick.whenButton(10),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.ALGAE_LOW;
        //             queuedControl.shoulderPosition = ShoulderPosition.ALGAE_LOW;
        //             queuedControl.elevatorPosition = ElevatorPosition.ALGAE_LOW;
        //         });
        // addRule("Algae In",
        //         leftJoystick.whenButton(11),
        //         ONCE_AND_HOLD,
        //         intake,
        //         () -> {intake.turnAlgaeNegative();});

        // addRule("Nudge Shoulder Up",
        //         leftJoystick.whenButton(12),
        //         ONCE,
        //         Set.of(shoulder),
        //         () -> shoulder.nudgeUp());
        // addRule("Nudge Shoulder Down",
        //         leftJoystick.whenButton(13),
        //         ONCE,
        //         Set.of(shoulder),
        //         () -> shoulder.nudgeDown());
        // addRule(
        //         "Prep Coral Ground",
        //         leftJoystick.whenButton(3),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.CORAL_GROUND;
        //             queuedControl.shoulderPosition = ShoulderPosition.CORAL_GROUND;
        //             queuedControl.elevatorPosition = ElevatorPosition.CORAL_GROUND;
        //         });
        // addRule(
        //         "Prep Algae Ground",
        //         boxopGamepad.whenButton(8),
        //         ONCE,
        //         Set.of(wrist, shoulder, elevator),
        //         () -> {
        //             queuedControl.wristPosition = WristPosition.ALGAE_GROUND;
        //             queuedControl.shoulderPosition = ShoulderPosition.ALGAE_GROUND;
        //             queuedControl.elevatorPosition = ElevatorPosition.ALGAE_GROUND;
        //         });

        // addRule(
        //         "Score L2 or L3 LEFT",
        //         rightJoystick.whenButton(1),
        //         ONCE_AND_HOLD,
        //         () ->
        //                 new AutoScore(
        //                         Vision.getTargetPositionLeftL2L3(),
        //                         swerveDrive,
        //                         wrist,
        //                         shoulder,
        //                         elevator,
        //                         queuedControl.elevatorPosition.getPosition(),
        //                         queuedControl.wristPosition.getPosition(),
        //                         queuedControl.shoulderPosition.getPosition()));
        // addRule(
        //         "Score L2 or L3 RIGHT",
        //         rightJoystick.whenButton(2),
        //         ONCE_AND_HOLD,
        //         () ->
        //                 new AutoScore(
        //                         Vision.getTargetPositionRightL2L3(),
        //                         swerveDrive,
        //                         wrist,
        //                         shoulder,
        //                         elevator,
        //                         queuedControl.elevatorPosition.getPosition(),
        //                         queuedControl.wristPosition.getPosition(),
        //                         queuedControl.shoulderPosition.getPosition()));
        // addRule(
        //         "Score L4 LEFT",
        //         rightJoystick.whenButton(3),
        //         ONCE_AND_HOLD,
        //         () ->
        //                 new AutoScore(
        //                         Vision.getTargetPositionLeftL4(),
        //                         swerveDrive,
        //                         wrist,
        //                         shoulder,
        //                         elevator,
        //                         queuedControl.elevatorPosition.getPosition(),
        //                         queuedControl.wristPosition.getPosition(),
        //                         queuedControl.shoulderPosition.getPosition()));
        // addRule(
        //         "Score L4 RIGHT",
        //         rightJoystick.whenButton(4),
        //         ONCE_AND_HOLD,
        //         () ->
        //                 new AutoScore(
        //                         Vision.getTargetPositionRightL4(),
        //                         swerveDrive,
        //                         wrist,
        //                         shoulder,
        //                         elevator,
        //                         queuedControl.elevatorPosition.getPosition(),
        //                         queuedControl.wristPosition.getPosition(),
        //                         queuedControl.shoulderPosition.getPosition()));
    }
}

package com.team766.robot.copy_2910;

import static com.team766.framework.RulePersistence.ONCE;
import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.copy_2910.mechanisms.*;
import com.team766.robot.copy_2910.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.copy_2910.mechanisms.Shoulder.ShoulderPosition;
import com.team766.robot.copy_2910.mechanisms.Wrist.WristPosition;
import com.team766.robot.copy_2910.procedures.AutoScore;
import com.team766.robot.copy_2910.procedures.IntakeCoral;
import com.team766.robot.copy_2910.procedures.OuttakeCoral;
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

        final JoystickReader leftJoystick = RobotProvider.instance.getJoystick(0);
        final JoystickReader rightJoystick = RobotProvider.instance.getJoystick(2);
        final JoystickReader boxopGamepad = RobotProvider.instance.getJoystick(3);

        QueuedControl queuedControl = new QueuedControl();
        queuedControl.wristPosition = WristPosition.L3;
        queuedControl.shoulderPosition = ShoulderPosition.L3;
        queuedControl.elevatorPosition = ElevatorPosition.L3;

        addRule("Intake Coral", leftJoystick.whenButton(1), ONCE_AND_HOLD, () -> new IntakeCoral(intake));
        addRule(
                "Outtake Coral",
                leftJoystick.whenButton(2),
                ONCE_AND_HOLD,
                () -> new OuttakeCoral(intake));

        addRule(
                "Apply queued positions",
                leftJoystick.whenButton(4),
                ONCE_AND_HOLD,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    wrist.setSetpoint(queuedControl.wristPosition.getPosition());
                    shoulder.setSetpoint(queuedControl.shoulderPosition.getPosition());
                    elevator.setPosition(queuedControl.elevatorPosition.getPosition());
                });

        addRule(
                "Prep L1 Coral",
                boxopGamepad.whenButton(1),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.L1;
                    queuedControl.shoulderPosition = ShoulderPosition.L1;
                    queuedControl.elevatorPosition = ElevatorPosition.L1;
                });
        addRule(
                "Prep L2 Coral",
                boxopGamepad.whenButton(2),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.L2;
                    queuedControl.shoulderPosition = ShoulderPosition.L2;
                    queuedControl.elevatorPosition = ElevatorPosition.L2;
                });
        addRule(
                "Prep L3 Coral",
                boxopGamepad.whenButton(3),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.L3;
                    queuedControl.shoulderPosition = ShoulderPosition.L3;
                    queuedControl.elevatorPosition = ElevatorPosition.L3;
                });
        addRule(
                "Prep L4 Coral",
                boxopGamepad.whenButton(4),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.L4;
                    queuedControl.shoulderPosition = ShoulderPosition.L4;
                    queuedControl.elevatorPosition = ElevatorPosition.L4;
                });
        addRule(
                "Prep Algae High",
                boxopGamepad.whenButton(5),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.ALGAE_HIGH;
                    queuedControl.shoulderPosition = ShoulderPosition.ALGAE_HIGH;
                    queuedControl.elevatorPosition = ElevatorPosition.ALGAE_HIGH;
                });
        addRule(
                "Prep Algae Low",
                boxopGamepad.whenButton(6),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.ALGAE_LOW;
                    queuedControl.shoulderPosition = ShoulderPosition.ALGAE_LOW;
                    queuedControl.elevatorPosition = ElevatorPosition.ALGAE_LOW;
                });
        addRule(
                "Prep Coral Ground",
                leftJoystick.whenButton(3),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.CORAL_GROUND;
                    queuedControl.shoulderPosition = ShoulderPosition.CORAL_GROUND;
                    queuedControl.elevatorPosition = ElevatorPosition.CORAL_GROUND;
                });
        addRule(
                "Prep Algae Ground",
                boxopGamepad.whenButton(8),
                ONCE,
                Set.of(wrist, shoulder, elevator),
                () -> {
                    queuedControl.wristPosition = WristPosition.ALGAE_GROUND;
                    queuedControl.shoulderPosition = ShoulderPosition.ALGAE_GROUND;
                    queuedControl.elevatorPosition = ElevatorPosition.ALGAE_GROUND;
                });

        addRule(
                "Score L2 or L3 LEFT",
                rightJoystick.whenButton(1),
                ONCE_AND_HOLD,
                () ->
                        new AutoScore(
                                Vision.getTargetPositionLeftL2L3(),
                                swerveDrive,
                                wrist,
                                shoulder,
                                elevator,
                                queuedControl.elevatorPosition.getPosition(),
                                queuedControl.wristPosition.getPosition(),
                                queuedControl.shoulderPosition.getPosition()));
        addRule(
                "Score L2 or L3 RIGHT",
                rightJoystick.whenButton(2),
                ONCE_AND_HOLD,
                () ->
                        new AutoScore(
                                Vision.getTargetPositionRightL2L3(),
                                swerveDrive,
                                wrist,
                                shoulder,
                                elevator,
                                queuedControl.elevatorPosition.getPosition(),
                                queuedControl.wristPosition.getPosition(),
                                queuedControl.shoulderPosition.getPosition()));
        addRule(
                "Score L4 LEFT",
                rightJoystick.whenButton(3),
                ONCE_AND_HOLD,
                () ->
                        new AutoScore(
                                Vision.getTargetPositionLeftL4(),
                                swerveDrive,
                                wrist,
                                shoulder,
                                elevator,
                                queuedControl.elevatorPosition.getPosition(),
                                queuedControl.wristPosition.getPosition(),
                                queuedControl.shoulderPosition.getPosition()));
        addRule(
                "Score L4 RIGHT",
                rightJoystick.whenButton(4),
                ONCE_AND_HOLD,
                () ->
                        new AutoScore(
                                Vision.getTargetPositionRightL4(),
                                swerveDrive,
                                wrist,
                                shoulder,
                                elevator,
                                queuedControl.elevatorPosition.getPosition(),
                                queuedControl.wristPosition.getPosition(),
                                queuedControl.shoulderPosition.getPosition()));
    }
}

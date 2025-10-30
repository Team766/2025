package com.team766.robot.gatorade;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleEngine;
import com.team766.framework.RuleGroup;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.DriverOI;
import com.team766.robot.common.mechanisms.*;
import com.team766.robot.gatorade.constants.InputConstants;
import com.team766.robot.gatorade.mechanisms.*;
import com.team766.robot.gatorade.procedures.*;
import java.util.Set;

/**
 * This class is the glue that binds the controls on the physical operator interface to the code
 * that allow control of the robot.
 */
public class OI extends RuleEngine {

    public record OIStatus(PlacementPosition placementPosition, GamePieceType gamePieceType)
            implements Status {}

    private PlacementPosition placementPosition = PlacementPosition.NONE;
    private GamePieceType gamePieceType = GamePieceType.CONE;

    public OI(SwerveDrive drive, Shoulder shoulder, Elevator elevator, Wrist wrist, Intake intake) {
        final JoystickReader leftJoystick =
                RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
        final JoystickReader rightJoystick =
                RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);
        final JoystickReader boxopGamepad =
                RobotProvider.instance.getJoystick(InputConstants.BOXOP_GAMEPAD);

        boxopGamepad.setAllAxisDeadzone(0.05);

        // Driver OI: take input from left, right joysticks.  control drive.
        addRules(new DriverOI(leftJoystick, rightJoystick, drive));

        addRule(
                        "Intake Out",
                        leftJoystick.whenButton(InputConstants.BUTTON_INTAKE_OUT),
                        intake,
                        () -> intake.out(gamePieceType))
                .withFinishedTriggeringProcedure(intake, () -> intake.stop());

        // Respond to boxop commands

        // first, check if the boxop is making a cone or cube selection
        addRule(
                "Select Cone",
                () -> boxopGamepad.getPOV() == InputConstants.POV_UP,
                ONCE,
                Set.of(),
                () -> {
                    gamePieceType = GamePieceType.CONE;
                    updateStatus();
                });
        addRule(
                "Select Cube",
                () -> boxopGamepad.getPOV() == InputConstants.POV_DOWN,
                ONCE,
                Set.of(),
                () -> {
                    gamePieceType = GamePieceType.CUBE;
                    updateStatus();
                });

        // look for button presses to queue placement of intake/wrist/elevator superstructure
        addRule(
                "Select none",
                boxopGamepad.whenButton(InputConstants.BUTTON_PLACEMENT_NONE),
                ONCE,
                Set.of(),
                () -> {
                    placementPosition = PlacementPosition.NONE;
                    updateStatus();
                });
        addRule(
                "Select low",
                boxopGamepad.whenButton(InputConstants.BUTTON_PLACEMENT_LOW),
                ONCE,
                Set.of(),
                () -> {
                    placementPosition = PlacementPosition.LOW_NODE;
                    updateStatus();
                });
        addRule(
                "Select mid",
                boxopGamepad.whenButton(InputConstants.BUTTON_PLACEMENT_MID),
                ONCE,
                Set.of(),
                () -> {
                    placementPosition = PlacementPosition.MID_NODE;
                    updateStatus();
                });
        addRule(
                "Select high",
                boxopGamepad.whenButton(InputConstants.BUTTON_PLACEMENT_HIGH),
                ONCE,
                Set.of(),
                () -> {
                    placementPosition = PlacementPosition.HIGH_NODE;
                    updateStatus();
                });
        addRule(
                "Select human",
                boxopGamepad.whenButton(InputConstants.BUTTON_PLACEMENT_HUMAN_PLAYER),
                ONCE,
                Set.of(),
                () -> {
                    placementPosition = PlacementPosition.HUMAN_PLAYER;
                    updateStatus();
                });

        // look for button hold to start intake, release to idle intake
        addRule(
                        "Intake In",
                        boxopGamepad.whenButton(InputConstants.BUTTON_INTAKE_IN),
                        intake,
                        () -> intake.in(gamePieceType))
                .withFinishedTriggeringProcedure(intake, () -> intake.idle(gamePieceType));

        addRule(
                "Intake Stop",
                boxopGamepad.whenButton(InputConstants.BUTTON_INTAKE_STOP),
                intake,
                () -> intake.stop());

        // look for button hold to extend intake/wrist/elevator superstructure,
        // release to retract
        addRule(
                        "Extend Wristvator",
                        boxopGamepad.whenButton(InputConstants.BUTTON_EXTEND_WRISTVATOR))
                .withOnTriggeringProcedure(
                        ONCE,
                        () ->
                                switch (placementPosition) {
                                    case NONE -> null;
                                    case LOW_NODE ->
                                            new ExtendWristvatorToLow(shoulder, elevator, wrist);
                                    case MID_NODE ->
                                            new ExtendWristvatorToMid(shoulder, elevator, wrist);
                                    case HIGH_NODE ->
                                            new ExtendWristvatorToHigh(shoulder, elevator, wrist);
                                    case HUMAN_PLAYER ->
                                            new ExtendToHumanWithIntake(
                                                    gamePieceType,
                                                    shoulder,
                                                    elevator,
                                                    wrist,
                                                    intake);
                                })
                .whenTriggering(
                        new RuleGroup() {
                            {
                                // look for manual nudges
                                // we only allow these if the extend elevator trigger is extended

                                // look for elevator nudges
                                addRule(
                                        "Elevator nudge",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.AXIS_ELEVATOR_MOVEMENT),
                                        REPEATEDLY,
                                        elevator,
                                        () -> {
                                            final double elevatorNudgeAxis =
                                                    -1
                                                            * boxopGamepad.getAxis(
                                                                    InputConstants
                                                                            .AXIS_ELEVATOR_MOVEMENT);
                                            // elevator.nudgeNoPID(elevatorNudgeAxis);
                                            if (elevatorNudgeAxis > 0) {
                                                elevator.nudgeUp();
                                            } else {
                                                elevator.nudgeDown();
                                            }
                                        });
                                // look for wrist nudges
                                addRule(
                                        "Wrist nudge",
                                        boxopGamepad.whenAxisMoved(
                                                InputConstants.AXIS_WRIST_MOVEMENT),
                                        REPEATEDLY,
                                        wrist,
                                        () -> {
                                            final double wristNudgeAxis =
                                                    -1
                                                            * boxopGamepad.getAxis(
                                                                    InputConstants
                                                                            .AXIS_WRIST_MOVEMENT);
                                            // wrist.nudgeNoPID(wristNudgeAxis);
                                            if (wristNudgeAxis > 0) {
                                                wrist.nudgeUp();
                                            } else {
                                                wrist.nudgeDown();
                                            }
                                        });
                            }
                        })
                .withFinishedTriggeringProcedure(
                        () ->
                                placementPosition == PlacementPosition.HUMAN_PLAYER
                                        ? new RetractWristvatorIdleIntake(
                                                gamePieceType, shoulder, elevator, wrist, intake)
                                        : new RetractWristvator(shoulder, elevator, wrist));
    }

    private void updateStatus() {
        publishStatus(new OIStatus(placementPosition, gamePieceType));
    }
}

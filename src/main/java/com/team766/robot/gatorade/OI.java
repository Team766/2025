package com.team766.robot.gatorade;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.Rule;
import com.team766.framework3.RuleEngine;
import com.team766.framework3.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.DriverOI;
import com.team766.robot.common.mechanisms.*;
import com.team766.robot.gatorade.constants.InputConstants;
import com.team766.robot.gatorade.mechanisms.*;
import com.team766.robot.gatorade.mechanisms.Intake.GamePieceType;
import com.team766.robot.gatorade.procedures.*;
import java.util.Set;

/**
 * This class is the glue that binds the controls on the physical operator interface to the code
 * that allow control of the robot.
 */
public class OI extends RuleEngine {

    public record OIStatus(GamePieceType gamePieceType, PlacementPosition placementPosition)
            implements Status {}

    PlacementPosition placementPosition = PlacementPosition.NONE;

    GamePieceType gamePieceType = GamePieceType.CONE;

    public OI(SwerveDrive drive, Arm arm, Intake intake) {
        final JoystickReader leftJoystick =
                RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
        final JoystickReader rightJoystick =
                RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);
        final JoystickReader boxopGamepad =
                RobotProvider.instance.getJoystick(InputConstants.BOXOP_GAMEPAD);

        // Driver OI: take input from left, right joysticks.  control drive.
        new DriverOI(this, leftJoystick, rightJoystick, drive);

        addRule(
                Rule.create(
                                "Intake Out",
                                () -> leftJoystick.getButton(InputConstants.BUTTON_INTAKE_OUT))
                        .onTriggering(
                                ONCE_AND_HOLD,
                                Set.of(intake),
                                () -> intake.requestIntake(gamePieceType, Intake.MotorState.OUT))
                        .onFinishedTriggering(
                                Set.of(intake),
                                () -> intake.requestIntake(gamePieceType, Intake.MotorState.STOP)));

        // Respond to boxop commands

        // first, check if the boxop is making a cone or cube selection
        addRule(
                Rule.create("Select Cone", () -> boxopGamepad.getPOV() == InputConstants.POV_UP)
                        .onTriggering(
                                ONCE,
                                Set.of(),
                                () -> {
                                    gamePieceType = GamePieceType.CONE;
                                    updateStatus();
                                }));
        addRule(
                Rule.create("Select Cube", () -> boxopGamepad.getPOV() == InputConstants.POV_DOWN)
                        .onTriggering(
                                ONCE,
                                Set.of(),
                                () -> {
                                    gamePieceType = GamePieceType.CUBE;
                                    updateStatus();
                                }));

        // look for button presses to queue placement of intake/wrist/elevator superstructure
        addRule(
                Rule.create(
                                "Select none",
                                () -> boxopGamepad.getButton(InputConstants.BUTTON_PLACEMENT_NONE))
                        .onTriggering(
                                ONCE,
                                Set.of(),
                                () -> {
                                    placementPosition = PlacementPosition.NONE;
                                    updateStatus();
                                }));
        addRule(
                Rule.create(
                                "Select low",
                                () -> boxopGamepad.getButton(InputConstants.BUTTON_PLACEMENT_LOW))
                        .onTriggering(
                                ONCE,
                                Set.of(),
                                () -> {
                                    placementPosition = PlacementPosition.LOW_NODE;
                                    updateStatus();
                                }));
        addRule(
                Rule.create(
                                "Select mid",
                                () -> boxopGamepad.getButton(InputConstants.BUTTON_PLACEMENT_MID))
                        .onTriggering(
                                ONCE,
                                Set.of(),
                                () -> {
                                    placementPosition = PlacementPosition.MID_NODE;
                                    updateStatus();
                                }));
        addRule(
                Rule.create(
                                "Select high",
                                () -> boxopGamepad.getButton(InputConstants.BUTTON_PLACEMENT_HIGH))
                        .onTriggering(
                                ONCE,
                                Set.of(),
                                () -> {
                                    placementPosition = PlacementPosition.HIGH_NODE;
                                    updateStatus();
                                }));
        addRule(
                Rule.create(
                                "Select human",
                                () ->
                                        boxopGamepad.getButton(
                                                InputConstants.BUTTON_PLACEMENT_HUMAN_PLAYER))
                        .onTriggering(
                                ONCE,
                                Set.of(),
                                () -> {
                                    placementPosition = PlacementPosition.HUMAN_PLAYER;
                                    updateStatus();
                                }));

        // look for button hold to start intake, release to idle intake
        addRule(
                Rule.create(
                                "Intake In",
                                () -> boxopGamepad.getButton(InputConstants.BUTTON_INTAKE_IN))
                        .onTriggering(
                                ONCE_AND_HOLD,
                                Set.of(intake),
                                () -> intake.requestIntake(gamePieceType, Intake.MotorState.IN))
                        .onFinishedTriggering(
                                Set.of(intake),
                                () -> intake.requestIntake(gamePieceType, Intake.MotorState.IDLE)));

        addRule(
                Rule.create(
                                "Intake Stop",
                                () -> boxopGamepad.getButton(InputConstants.BUTTON_INTAKE_STOP))
                        .onTriggering(
                                ONCE,
                                Set.of(intake),
                                () -> intake.requestIntake(gamePieceType, Intake.MotorState.STOP)));

        // look for button hold to extend intake/wrist/elevator superstructure,
        // release to retract
        addRule(
                Rule.create(
                                "Extend Wristvator",
                                () ->
                                        boxopGamepad.getButton(
                                                InputConstants.BUTTON_EXTEND_WRISTVATOR))
                        .onTriggering(
                                ONCE,
                                Set.of(arm),
                                () -> {
                                    if (placementPosition != PlacementPosition.NONE) {
                                        arm.requestExtended(placementPosition, gamePieceType);
                                    }
                                })
                        .onFinishedTriggering(
                                Set.of(arm, intake),
                                () -> {
                                    arm.requestRetracted();
                                    if (placementPosition == PlacementPosition.HUMAN_PLAYER) {
                                        intake.requestIntake(gamePieceType, Intake.MotorState.IDLE);
                                    }
                                }));

        // look for manual nudges
        // we only allow these if the extend elevator trigger is extended

        boxopGamepad.setAllAxisDeadzone(0.05);

        // look for elevator nudges
        addRule(
                Rule.create(
                                "Elevator nudge",
                                () ->
                                        boxopGamepad.getButton(
                                                        InputConstants.BUTTON_EXTEND_WRISTVATOR)
                                                && boxopGamepad.isAxisMoved(
                                                        InputConstants.AXIS_ELEVATOR_MOVEMENT))
                        .onTriggering(
                                REPEATEDLY,
                                Set.of(arm),
                                () -> {
                                    final double elevatorNudgeAxis =
                                            -1
                                                    * boxopGamepad.getAxis(
                                                            InputConstants.AXIS_ELEVATOR_MOVEMENT);
                                    // elevator.setRequest(new
                                    // Elevator.NudgeNoPID(elevatorNudgeAxis)));
                                    if (elevatorNudgeAxis > 0) {
                                        arm.requestNudgeElevatorUp();
                                    } else {
                                        arm.requestNudgeElevatorDown();
                                    }
                                }));
        // look for wrist nudges
        addRule(
                Rule.create(
                                "Elevator nudge",
                                () ->
                                        boxopGamepad.getButton(
                                                        InputConstants.BUTTON_EXTEND_WRISTVATOR)
                                                && boxopGamepad.isAxisMoved(
                                                        InputConstants.AXIS_WRIST_MOVEMENT))
                        .onTriggering(
                                REPEATEDLY,
                                Set.of(arm),
                                () -> {
                                    final double wristNudgeAxis =
                                            -1
                                                    * boxopGamepad.getAxis(
                                                            InputConstants.AXIS_WRIST_MOVEMENT);
                                    // wrist.setRequest(new Wrist.NudgeNoPID(wristNudgeAxis));
                                    if (wristNudgeAxis > 0) {
                                        arm.requestNudgeWristUp();
                                    } else {
                                        arm.requestNudgeWristDown();
                                    }
                                }));
    }

    private void updateStatus() {
        publishStatus(new OIStatus(gamePieceType, placementPosition));
    }
}

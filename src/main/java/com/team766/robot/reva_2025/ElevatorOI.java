package com.team766.robot.reva_2025;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.framework3.Conditions.LogicalAnd;
import com.team766.hal.JoystickReader;
import com.team766.robot.gatorade.mechanisms.Elevator;
import com.team766.robot.reva_2025.constants.InputConstants;

public class ElevatorOI extends RuleGroup {
    public ElevatorOI(JoystickReader gamePad, Elevator elevator) {
        addRule(
            "Move Elevator L1",
            new LogicalAnd(gamePad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L1), gamePad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
            ONCE,
            elevator,
            () -> {
                elevator.setPosition(Elevator.ELEVATOR_L1);
            });

        addRule(
            "Move Elevator L2",
            new LogicalAnd(gamePad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L2), gamePad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
            ONCE,
            elevator,
            () -> {
                elevator.setPosition(Elevator.ELEVATOR_L2);
            });
        addRule(
            "Move Elevator L3",
            new LogicalAnd(gamePad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L3), gamePad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
            ONCE,
            elevator,
            () -> {
                elevator.setPosition(Elevator.ELEVATOR_L3);
            });
        addRule(
            "Move Elevator L4",
            new LogicalAnd(gamePad.whenButton(InputConstants.BUTTON_ELEVATOR_WRIST_L4), gamePad.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER)),
            ONCE,
            elevator,
            () -> {
                elevator.setPosition(Elevator.ELEVATOR_L4);
            });
    }
}

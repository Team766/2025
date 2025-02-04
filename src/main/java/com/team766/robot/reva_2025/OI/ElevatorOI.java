package com.team766.robot.reva_2025.OI;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.mechanisms.Elevator;

public class ElevatorOI extends RuleGroup {
    public ElevatorOI(JoystickReader gamePad, Elevator elevator) {
        addRule(
                "Elevator Up",
                gamePad.whenButton(InputConstants.GAMEPAD_A_BUTTON),
                ONCE,
                elevator,
                () -> {
                    elevator.nudgeUp();
                    ;
                });

        addRule(
                "Elevator Down",
                gamePad.whenButton(InputConstants.GAMEPAD_Y_BUTTON),
                ONCE,
                elevator,
                () -> {
                    elevator.nudgeDown();
                    ;
                });
    }
}

package com.team766.robot.tutorial;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.tutorial.mechanisms.*;
import com.team766.robot.tutorial.procedures.*;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    public OI(Elevator elevator) {
        final JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver control rules here.

        addRule(
                "Elevator Nudge Up",
                joystick2.whenButton(1),
                () -> new IncrementElevatorPosition(elevator));
        addRule(
                "Elevator Nudge Down",
                joystick2.whenButton(2),
                () -> new DecrementElevatorPosition(elevator));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

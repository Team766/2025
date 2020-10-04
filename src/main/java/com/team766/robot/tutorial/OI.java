package com.team766.robot.tutorial;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.tutorial.mechanisms.*;
import com.team766.robot.tutorial.procedures.*;
import java.util.Set;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    public OI() {
        final JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver control rules here.
        addRule(
                "Log driver controls",
                UNCONDITIONAL,
                Set.of(),
                () ->
                        log(
                                "J0 A0: "
                                        + joystick0.getAxis(0)
                                        + "  J0 A1: "
                                        + joystick0.getAxis(1)
                                        + "  J1 A0: "
                                        + joystick1.getAxis(0)
                                        + "  J1 A1: "
                                        + joystick1.getAxis(1)
                                        + "  J0 B1: "
                                        + joystick0.getButton(1)
                                        + "  J0 B2: "
                                        + joystick0.getButton(2)
                                        + "  J0 B3: "
                                        + joystick0.getButton(3)));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

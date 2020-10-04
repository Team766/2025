package com.team766.robot.tutorial;

import static com.team766.framework.RulePersistence.REPEATEDLY;

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
    public OI(Drive drive, Launcher launcher, Intake intake) {
        final JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver control rules here.
        addRule(
                "Drive robot",
                UNCONDITIONAL,
                REPEATEDLY,
                drive,
                () -> drive.setArcadeDrivePower(joystick0.getAxis(1), joystick1.getAxis(0)));

        addRule("Launch", joystick0.whenButton(1), () -> new Launch(launcher));

        addRule("Start Intake", joystick1.whenButton(2), () -> new StartIntake(intake));
        addRule("Stop Intake", joystick1.whenButton(3), () -> new StopIntake(intake));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

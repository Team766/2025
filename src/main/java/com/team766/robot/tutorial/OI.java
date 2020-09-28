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
    public OI(Drive drive, Intake intake, Launcher launcher) {
        final JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver control rules here.
        addRule(
                "Drive controls",
                UNCONDITIONAL,
                drive,
                () -> drive.setArcadeDrive(joystick0.getAxis(1), joystick1.getAxis(0)));

        addRule("Launch", joystick0.whenButton(1), () -> new Launch(launcher));

        addRule(
                "Start intake",
                joystick1.whenButton(2),
                intake,
                () -> {
                    intake.setIntakeArm(true);
                    intake.setIntakePower(1.0);
                });
        addRule(
                "Stop intake",
                joystick1.whenButton(3),
                intake,
                () -> {
                    intake.setIntakeArm(false);
                    intake.setIntakePower(0.0);
                });
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

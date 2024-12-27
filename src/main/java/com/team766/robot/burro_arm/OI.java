package com.team766.robot.burro_arm;

import static com.team766.framework3.RulePersistence.ONCE;
import static com.team766.framework3.RulePersistence.REPEATEDLY;
import static com.team766.robot.burro_arm.constants.InputConstants.*;

import com.team766.framework3.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.burro_arm.mechanisms.*;
import com.team766.robot.burro_arm.procedures.*;
import com.team766.robot.common.mechanisms.BurroDrive;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    private JoystickReader joystick0;
    private JoystickReader joystick1;
    private JoystickReader joystick2;

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }

    public OI(BurroDrive drive, Arm arm, Gripper gripper) {
        joystick0 = RobotProvider.instance.getJoystick(0);
        joystick1 = RobotProvider.instance.getJoystick(1);
        joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver controls here.

        addRule(
                "Drive Robot",
                () -> true,
                REPEATEDLY,
                drive,
                () ->
                        drive.requestArcadeDrive(
                                -joystick0.getAxis(AXIS_FORWARD_BACKWARD) * 0.5,
                                -joystick0.getAxis(AXIS_TURN) * 0.3));

        addRule(
                "Arm Up",
                joystick0.whenButton(BUTTON_ARM_UP),
                ONCE,
                arm,
                () -> arm.requestNudgeUp());
        addRule(
                "Arm Down",
                joystick0.whenButton(BUTTON_ARM_DOWN),
                ONCE,
                arm,
                () -> arm.requestNudgeDown());

        addRule(
                "Intake",
                joystick0.whenButton(BUTTON_INTAKE),
                gripper,
                () -> gripper.requestIntake());
        addRule(
                "Outtake",
                joystick0.whenButton(BUTTON_OUTTAKE),
                gripper,
                () -> gripper.requestOuttake());
    }
}

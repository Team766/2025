package com.team766.robot.burro_arm;

import static com.team766.framework.RulePersistence.REPEATEDLY;
import static com.team766.robot.burro_arm.constants.InputConstants.*;

import com.team766.framework.RuleEngine;
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
    public OI(BurroDrive drive, Arm arm, Gripper gripper) {
        JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver controls here.

        addRule(
                "Drive Robot",
                UNCONDITIONAL,
                REPEATEDLY,
                drive,
                () -> drive.drive(-joystick0.getAxis(1) * 0.5, -joystick0.getAxis(2) * 0.3));

        addRule(
                "Arm Up",
                joystick0.whenButton(BUTTON_ARM_UP),
                arm,
                () ->
                        arm.setAngle(
                                getStatusOrThrow(Arm.ArmStatus.class).angle()
                                        + NUDGE_UP_INCREMENT));
        addRule(
                "Arm Down",
                joystick0.whenButton(BUTTON_ARM_DOWN),
                arm,
                () ->
                        arm.setAngle(
                                getStatusOrThrow(Arm.ArmStatus.class).angle()
                                        - NUDGE_DOWN_INCREMENT));

        addRule(
                "Gripper Intake",
                joystick0.whenButton(BUTTON_INTAKE),
                gripper,
                () -> gripper.intake());
        addRule(
                "Gripper Outtake",
                joystick0.whenButton(BUTTON_OUTTAKE),
                gripper,
                () -> gripper.outtake());
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

package com.team766.robot.burro_elevator;

import static com.team766.framework3.RulePersistence.ONCE;
import static com.team766.framework3.RulePersistence.REPEATEDLY;
import static com.team766.robot.burro_elevator.constants.InputConstants.*;

import com.team766.framework3.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.burro_arm.mechanisms.Gripper;
import com.team766.robot.burro_elevator.mechanisms.*;
import com.team766.robot.burro_elevator.procedures.*;
import com.team766.robot.common.mechanisms.BurroDrive;
import java.util.Set;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    private JoystickReader joystick0;
    private JoystickReader joystick1;
    private JoystickReader joystick2;

    public OI(BurroDrive drive, Elevator elevator, Gripper gripper) {
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
                        new BurroDrive.ArcadeDriveRequest(
                                -joystick0.getAxis(AXIS_FORWARD_BACKWARD) * 0.5,
                                -joystick0.getAxis(AXIS_TURN) * 0.5));

        addRule(
                "Elevator Up",
                joystick0.whenButton(BUTTON_ELEVATOR_UP),
                ONCE,
                Set.of(elevator),
                () -> elevator.setRequestToNudgeUp());
        addRule(
                "Elevator Down",
                joystick0.whenButton(BUTTON_ELEVATOR_DOWN),
                ONCE,
                Set.of(elevator),
                () -> elevator.setRequestToNudgeDown());

        addRule(
                "Intake",
                joystick0.whenButton(BUTTON_INTAKE),
                gripper,
                () -> Gripper.requestForIntake());
        addRule(
                "Outtake",
                joystick0.whenButton(BUTTON_OUTTAKE),
                gripper,
                () -> Gripper.requestForOuttake());
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

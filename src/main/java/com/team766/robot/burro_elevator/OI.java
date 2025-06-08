package com.team766.robot.burro_elevator;

import static com.team766.framework.RulePersistence.REPEATEDLY;
import static com.team766.robot.burro_elevator.constants.InputConstants.*;

import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.burro_arm.mechanisms.Gripper;
import com.team766.robot.burro_elevator.mechanisms.*;
import com.team766.robot.burro_elevator.procedures.*;
import com.team766.robot.common.mechanisms.BurroDrive;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    public OI(BurroDrive drive, Elevator elevator, Gripper gripper) {
        JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver controls here.

        addRule(
                "Drive Robot",
                UNCONDITIONAL,
                REPEATEDLY,
                drive,
                () ->
                        drive.drive(
                                -joystick0.getAxis(AXIS_FORWARD_BACKWARD) * 0.5,
                                -joystick0.getAxis(AXIS_TURN) * 0.5));

        addRule(
                "Elevator Up",
                joystick0.whenButton(BUTTON_ELEVATOR_UP),
                elevator,
                () ->
                        elevator.setPosition(
                                getStatusOrThrow(Elevator.ElevatorStatus.class).position()
                                        + NUDGE_UP_INCREMENT));
        addRule(
                "Elevator Down",
                joystick0.whenButton(BUTTON_ELEVATOR_DOWN),
                elevator,
                () ->
                        elevator.setPosition(
                                getStatusOrThrow(Elevator.ElevatorStatus.class).position()
                                        - NUDGE_DOWN_INCREMENT));

        // addRule("Elevator Up", joystick0.whenButton(BUTTON_ELEVATOR_UP))
        //         .withOnTriggeringProcedure(ONCE, elevator, () -> elevator.setPower(0.2))
        //         .withFinishedTriggeringProcedure(elevator, () -> elevator.setPower(0));
        // addRule("Elevator Down", joystick0.whenButton(BUTTON_ELEVATOR_DOWN))
        //         .withOnTriggeringProcedure(ONCE, elevator, () -> elevator.setPower(-0.2))
        //         .withFinishedTriggeringProcedure(elevator, () -> elevator.setPower(0));

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

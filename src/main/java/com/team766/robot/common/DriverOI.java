package com.team766.robot.common;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.Conditions;
import com.team766.framework3.Rule;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;
import java.util.Set;

public class DriverOI extends RuleGroup {
    public DriverOI(JoystickReader leftJoystick, JoystickReader rightJoystick, SwerveDrive drive) {
        leftJoystick.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);
        rightJoystick.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);

        addRule(
                Rule.create(
                                "Reset Gyro",
                                () -> leftJoystick.getButton(InputConstants.BUTTON_RESET_GYRO))
                        .onTriggering(ONCE, Set.of(drive), () -> drive.resetGyro()));
        addRule(
                Rule.create(
                                "Reset Pos",
                                () -> leftJoystick.getButton(InputConstants.BUTTON_RESET_POS))
                        .onTriggering(ONCE, Set.of(drive), () -> drive.resetCurrentPosition()));

        // Sets the wheels to the cross position if the cross button is pressed
        addRule(
                Rule.create(
                                "Cross Wheels",
                                new Conditions.Toggle(
                                        () ->
                                                rightJoystick.getButton(
                                                        InputConstants.BUTTON_CROSS_WHEELS)))
                        .onTriggering(ONCE_AND_HOLD, Set.of(drive), () -> drive.requestStop()));

        // Moves the robot if there are joystick inputs
        addRule(
                Rule.create(
                                "Joysticks moved",
                                () ->
                                        leftJoystick.isAxisMoved(
                                                        InputConstants.AXIS_FORWARD_BACKWARD)
                                                || leftJoystick.isAxisMoved(
                                                        InputConstants.AXIS_LEFT_RIGHT)
                                                || rightJoystick.isAxisMoved(
                                                        InputConstants.AXIS_LEFT_RIGHT))
                        .onTriggering(
                                REPEATEDLY,
                                Set.of(drive),
                                () -> {
                                    // For fwd/rv
                                    // Negative because forward is negative in driver station
                                    final double leftJoystickX =
                                            -leftJoystick.getAxis(
                                                            InputConstants.AXIS_FORWARD_BACKWARD)
                                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                                    // For left/right
                                    // Negative because left is negative in driver station
                                    final double leftJoystickY =
                                            -leftJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT)
                                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                                    // For steer
                                    // Negative because left is negative in driver station
                                    final double rightJoystickY =
                                            -rightJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT)
                                                    * ControlConstants.MAX_ROTATIONAL_VELOCITY;
                                    // If a button is pressed, drive is just fine adjustment
                                    final double drivingCoefficient =
                                            rightJoystick.getButton(
                                                            InputConstants.BUTTON_FINE_DRIVING)
                                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                                    : 1;
                                    drive.requestFieldOrientedVelocity(
                                            drivingCoefficient
                                                    * curvedJoystickPower(
                                                            leftJoystickX,
                                                            ControlConstants
                                                                    .TRANSLATIONAL_CURVE_POWER),
                                            drivingCoefficient
                                                    * curvedJoystickPower(
                                                            leftJoystickY,
                                                            ControlConstants
                                                                    .TRANSLATIONAL_CURVE_POWER),
                                            drivingCoefficient
                                                    * curvedJoystickPower(
                                                            rightJoystickY,
                                                            ControlConstants
                                                                    .ROTATIONAL_CURVE_POWER));
                                }));
    }

    private static double curvedJoystickPower(double value, double power) {
        return Math.signum(value) * Math.pow(Math.abs(value), power);
    }
}

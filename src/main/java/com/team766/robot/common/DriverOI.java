package com.team766.robot.common;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.math.Maths;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;

public class DriverOI extends RuleGroup {
    public DriverOI(JoystickReader leftJoystick, JoystickReader rightJoystick, SwerveDrive drive) {
        leftJoystick.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);
        rightJoystick.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);

        addRule(
                "Reset Gyro",
                leftJoystick.whenButton(InputConstants.BUTTON_RESET_GYRO),
                ONCE,
                drive,
                () -> drive.resetGyro());
        addRule(
                "Reset Pos",
                leftJoystick.whenButton(InputConstants.BUTTON_RESET_POS),
                ONCE,
                drive,
                () -> drive.resetCurrentPosition());

        // Sets the wheels to the cross position if the cross button is pressed
        // addRule(
        //         "Cross Wheels",
        //         new
        // Conditions.Toggle(rightJoystick.whenButton(InputConstants.BUTTON_CROSS_WHEELS)),
        //         drive,
        //         () -> drive.stopDrive());

        // Moves the robot if there are joystick inputs
        addRule(
                "Joysticks moved",
                () ->
                        leftJoystick.isAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD)
                                || leftJoystick.isAxisMoved(InputConstants.AXIS_LEFT_RIGHT)
                                || leftJoystick.isAxisMoved(
                                        InputConstants.GAMEPAD_RIGHT_STICK_XAXIS)
                                || rightJoystick.isAxisMoved(InputConstants.AXIS_LEFT_RIGHT),
                REPEATEDLY,
                drive,
                () -> {
                    // For fwd/rv
                    // Negative because forward is negative in driver station
                    final double leftJoystickX =
                            -killDeadzone(
                                    curvedJoystickPower(
                                            leftJoystick.getAxis(
                                                    InputConstants.AXIS_FORWARD_BACKWARD),
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER));
                    // For left/right
                    // Negative because left is negative in driver station
                    final double leftJoystickY =
                            -killDeadzone(
                                    curvedJoystickPower(
                                            leftJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT),
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER));
                    // For steer
                    // Negative because left is negative in driver station
                    final double rightJoystickY =
                            -killDeadzone(
                                    curvedJoystickPower(
                                            Maths.absMax(
                                                    rightJoystick.getAxis(
                                                            InputConstants.AXIS_LEFT_RIGHT),
                                                    leftJoystick.getAxis(
                                                            InputConstants
                                                                    .GAMEPAD_RIGHT_STICK_XAXIS)),
                                            ControlConstants.ROTATIONAL_CURVE_POWER));
                    // If a button is pressed, drive is just fine adjustment
                    final double drivingCoefficient =
                            // rightJoystick.getButton(InputConstants.BUTTON_FINE_DRIVING)
                            leftJoystick.getAxis(InputConstants.GAMEPAD_RIGHT_TRIGGER) < 0.5
                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                    : 1;
                    // drive.controlAllianceOriented(
                    drive.controlRobotOriented(
                            drivingCoefficient
                                    * leftJoystickX
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY,
                            drivingCoefficient
                                    * leftJoystickY
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY,
                            drivingCoefficient
                                    * rightJoystickY
                                    * ControlConstants.MAX_ROTATIONAL_VELOCITY);
                });
    }

    private static double killDeadzone(double value) {
        if (value < 0) {
            value -= 0.15;
        }
        if (value > 0) {
            value += 0.15;
        }
        return value;
    }

    private static double curvedJoystickPower(double value, double power) {
        return Math.signum(value) * Math.pow(Math.abs(value), power);
    }
}

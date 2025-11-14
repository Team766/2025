package com.team766.robot.outlaw.bearbot;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.outlaw.bearbot.constants.InputConstants;

public class DriverOI extends RuleGroup {
    public DriverOI(JoystickReader driverController, SwerveDrive drive) {
        // Only set deadzones on axes meant for controlling the robot motion
        driverController.setAxisDeadzone(InputConstants.LEFTSTICK_AXIS_LEFT_RIGHT,ControlConstants.JOYSTICK_DEADZONE);
        driverController.setAxisDeadzone(InputConstants.LEFTSTICK_AXIS_FORWARD_BACKWARD,ControlConstants.JOYSTICK_DEADZONE);
        driverController.setAxisDeadzone(InputConstants.RIGHTSTICK_AXIS_LEFT_RIGHT,ControlConstants.JOYSTICK_DEADZONE);
        driverController.setAxisDeadzone(InputConstants.RIGHTSTICK_AXIS_LEFT_RIGHT,ControlConstants.JOYSTICK_DEADZONE);

        addRule(
                "Reset Gyro",
                driverController.whenButton(InputConstants.BUTTON_RESET_GYRO),
                ONCE,
                drive,
                () -> drive.resetGyro());
 /*        addRule(
                "Reset Pos",
                driverController.whenButton(InputConstants.BUTTON_RESET_POS),
                ONCE,
                drive,
                () -> drive.resetCurrentPosition());*/

        // Moves the robot if there are joystick inputs
        addRule(
                "Joysticks moved",
                () ->
                        driverController.isAxisMoved(InputConstants.LEFTSTICK_AXIS_FORWARD_BACKWARD)
                        || driverController.isAxisMoved(
                                        InputConstants.LEFTSTICK_AXIS_LEFT_RIGHT)
                                || driverController.isAxisMoved(
                                        InputConstants.RIGHTSTICK_AXIS_LEFT_RIGHT),
                REPEATEDLY,
                drive,
                () -> {
                    // For fwd/rv
                    // Negative because forward is negative in driver station
                    final double leftStickX =
                            -driverController.getAxis(
                                            InputConstants.LEFTSTICK_AXIS_FORWARD_BACKWARD)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For left/right
                    // Negative because left is negative in driver station
                    final double leftStickY =
                            -driverController.getAxis(InputConstants.LEFTSTICK_AXIS_LEFT_RIGHT)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For steer
                    // Negative because left is negative in driver station
                    final double rightStickY =
                            -driverController.getAxis(InputConstants.RIGHTSTICK_AXIS_LEFT_RIGHT)
                                    * ControlConstants.MAX_ROTATIONAL_VELOCITY;
                    // If a button is pressed, drive is just fine adjustment
                    final double drivingCoefficient =
                            driverController.getButton(InputConstants.BUTTON_FINE_DRIVING)
                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                    : 1;
                    drive.controlAllianceOriented(
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            leftStickX, ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            leftStickY, ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            rightStickY, ControlConstants.ROTATIONAL_CURVE_POWER));
                });
    }

    private static double curvedJoystickPower(double value, double power) {
        return Math.signum(value) * Math.pow(Math.abs(value), power);
    }
}

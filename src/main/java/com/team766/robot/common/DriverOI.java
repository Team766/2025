package com.team766.robot.common;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;

public class DriverOI extends RuleGroup {
    public DriverOI(JoystickReader driverGamepad, SwerveDrive drive) {
        driverGamepad.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);

        addRule(
                "Reset Gyro",
                driverGamepad.whenButton(InputConstants.BUTTON_RESET_GYRO),
                ONCE,
                drive,
                () -> drive.resetGyro());
        addRule(
                "Reset Pos",
                driverGamepad.whenButton(InputConstants.BUTTON_RESET_POS),
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
        addRule( // QUESTION: do i make these the gamepad joystick x/y or change axis l/r in inputconstants
                "Joysticks moved", 
                () ->
                        driverGamepad.isAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD)
                                || driverGamepad.isAxisMoved(InputConstants.AXIS_LEFT_RIGHT),
                REPEATEDLY,
                drive,
                () -> {
                    // For fwd/rv
                    // Negative because forward is negative in driver station
                    final double driverGamepadLeftX =
                            -driverGamepad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For left/right
                    // Negative because left is negative in driver station
                    final double driverGamepadLeftY =
                            -driverGamepad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_XAXIS)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For steer
                    // Negative because left is negative in driver station
                    final double driverGamepadX =
                            -driverGamepad.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_XAXIS)
                                    * ControlConstants.MAX_ROTATIONAL_VELOCITY;
                    // If a button is pressed, drive is just fine adjustment
                    final double drivingCoefficient =
                            driverGamepad.getButton(InputConstants.BUTTON_FINE_DRIVING)
                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                    : 1;
                    drive.controlAllianceOriented(
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            driverGamepadX,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            driverGamepadY,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            rightJoystickY,
                                            ControlConstants.ROTATIONAL_CURVE_POWER));
                });
    }

    private static double curvedJoystickPower(double value, double power) {
        return Math.signum(value) * Math.pow(Math.abs(value), power);
    }
}

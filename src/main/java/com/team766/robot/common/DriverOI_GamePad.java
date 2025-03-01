package com.team766.robot.common;

import static com.team766.framework3.RulePersistence.ONCE;
import static com.team766.framework3.RulePersistence.REPEATEDLY;

import com.team766.framework3.Conditions;
import com.team766.framework3.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;

public class DriverOI_GamePad extends RuleGroup {
    public DriverOI_GamePad(JoystickReader gamePad, SwerveDrive drive) {
        gamePad.setAllAxisDeadzone(ControlConstants.GAMEPAD_DEADZONE);

        addRule(
                "Reset Gyro",
                gamePad.whenButton(InputConstants.GAMEPAD_BUTTON_RESET_GYRO),
                ONCE,
                drive,
                () -> drive.resetGyro());
        addRule(
                "Reset Pos",
                gamePad.whenButton(InputConstants.GAMEPAD_BUTTON_RESET_POS),
                ONCE,
                drive,
                () -> drive.resetCurrentPosition());

        // Sets the wheels to the cross position if the cross button is pressed
        addRule(
                "Cross Wheels",
                new Conditions.Toggle(
                        gamePad.whenButton(InputConstants.GAMEPAD_BUTTON_CROSS_WHEELS)),
                drive,
                () -> drive.stopDrive());

        // Moves the robot if there are joystick inputs
        addRule(
                "Joysticks moved",
                () ->
                        gamePad.isAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_YAXIS)
                                || gamePad.isAxisMoved(InputConstants.GAMEPAD_RIGHT_STICK_XAXIS)
                                || gamePad.isAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_XAXIS),
                REPEATEDLY,
                drive,
                () -> {
                    // For fwd/rv
                    // Negative because forward is negative in driver station
                    final double gamePadLeftX =
                            -gamePad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For left/right
                    // Negative because left is negative in driver station
                    final double gamePadLeftY =
                            -gamePad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_XAXIS)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For steer
                    // Negative because left is negative in driver station
                    final double gamePadRightY =
                            -gamePad.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_XAXIS)
                                    * ControlConstants.MAX_ROTATIONAL_VELOCITY;
                    // If a button is pressed, drive is just fine adjustment
                    final double drivingCoefficient =
                            gamePad.getButton(InputConstants.GAMEPAD_BUTTON_FINE_DRIVING)
                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                    : 1;
                    drive.controlAllianceOriented(
                            drivingCoefficient
                                    * curvedGamePadPower(
                                            gamePadLeftX,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedGamePadPower(
                                            gamePadLeftY,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedGamePadPower(
                                            gamePadRightY,
                                            ControlConstants.ROTATIONAL_CURVE_POWER));
                });
    }

    private static double curvedGamePadPower(double value, double power) {
        return Math.signum(value) * Math.pow(Math.abs(value), power);
    }
}

package com.team766.robot.copy_2910;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.copy_2910.mechanisms.*;

public class DriverOI extends RuleGroup {
    public DriverOI(JoystickReader gamepad, SwerveDrive drive) {
        gamepad.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);
        // TODO: make sure stick click works well
        addRule(
                "Reset Gyro",
                gamepad.whenButton(InputConstants.GAMEPAD_BACK_BUTTON),
                ONCE,
                drive,
                () -> drive.resetGyro());

        // addRule(
        //         "Reset Pos",
        //         gamepad.whenButton(InputConstants.GAMEPAD_RIGHT_STICK_CLICK),
        //         ONCE,
        //         drive,
        //         () -> drive.resetCurrentPosition());

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
                        gamepad.isAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_XAXIS)
                                || gamepad.isAxisMoved(InputConstants.GAMEPAD_LEFT_STICK_YAXIS)
                                || gamepad.isAxisMoved(InputConstants.GAMEPAD_RIGHT_STICK_XAXIS),
                REPEATEDLY,
                drive,
                () -> {
                    // For fwd/rv
                    // Negative because forward is negative in driver station
                    final double leftJoystickX =
                            -gamepad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_YAXIS)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For left/right
                    // Negative because left is negative in driver station
                    final double leftJoystickY =
                            -gamepad.getAxis(InputConstants.GAMEPAD_LEFT_STICK_XAXIS)
                                    * ControlConstants.MAX_TRANSLATIONAL_VELOCITY;
                    // For steer
                    // Negative because left is negative in driver station
                    final double rightJoystickY =
                            -gamepad.getAxis(InputConstants.GAMEPAD_RIGHT_STICK_XAXIS)
                                    * ControlConstants.MAX_ROTATIONAL_VELOCITY;
                    // If a button is pressed, drive is just fine adjustment
                    final double drivingCoefficient =
                            gamepad.getButton(InputConstants.BUTTON_FINE_DRIVING)
                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                    : 1;
                    drive.controlAllianceOriented(
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            leftJoystickX,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            leftJoystickY,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            rightJoystickY,
                                            ControlConstants.ROTATIONAL_CURVE_POWER));
                });
    }

    private static double curvedJoystickPower(double value, double power) {
        return Math.signum(value) * Math.pow(Math.abs(value), power);
        // TODO: tune all of this to work well w/ controller joysticks
    }
}

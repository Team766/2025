package com.team766.robot.reva;

import static com.team766.framework.RulePersistence.*;

import com.team766.framework.Conditions;
import com.team766.framework.RuleGroup;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.constants.InputConstants;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shoulder;
import com.team766.robot.reva.procedures.DriverShootNow;
import com.team766.robot.reva.procedures.DriverShootVelocityAndIntake;

public class DriverOI extends RuleGroup {
    public DriverOI(
            JoystickReader leftJoystick,
            JoystickReader rightJoystick,
            SwerveDrive drive,
            Shoulder shoulder,
            Intake intake) {
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

        addRule(
                "Cross wheels",
                new Conditions.Toggle(rightJoystick.whenButton(InputConstants.BUTTON_CROSS_WHEELS)),
                drive,
                () -> drive.stopDrive());

        addRule(
                "Target Shooter",
                leftJoystick.whenButton(InputConstants.BUTTON_TARGET_SHOOTER),
                () -> new DriverShootNow(drive, shoulder, intake));

        addRule(
                "Start Shooting",
                rightJoystick.whenButton(InputConstants.BUTTON_START_SHOOTING_PROCEDURE),
                () -> new DriverShootVelocityAndIntake(intake));

        // Moves the robot if there are joystick inputs
        addRule(
                "Move robot",
                () ->
                        leftJoystick.isAxisMoved(InputConstants.AXIS_FORWARD_BACKWARD)
                                || leftJoystick.isAxisMoved(InputConstants.AXIS_LEFT_RIGHT)
                                || rightJoystick.isAxisMoved(InputConstants.AXIS_LEFT_RIGHT),
                REPEATEDLY,
                drive,
                () -> {
                    // If a button is pressed, drive is just fine adjustment
                    final double drivingCoefficient =
                            rightJoystick.getButton(InputConstants.BUTTON_FINE_DRIVING)
                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                    : 1;
                    // For fwd/rv
                    // Negative because forward is negative in driver station
                    final double leftJoystickX =
                            -leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD)
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
    }
}

package com.team766.robot.reva;

import static com.team766.framework3.RulePersistence.*;

import com.team766.framework3.Conditions;
import com.team766.hal.JoystickReader;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.constants.InputConstants;
import com.team766.robot.reva.mechanisms.ArmAndClimber;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.procedures.DriverShootNow;
import com.team766.robot.reva.procedures.DriverShootVelocityAndIntake;
import java.util.Set;

public class DriverOI {
    public DriverOI(
            OI oi,
            JoystickReader leftJoystick,
            JoystickReader rightJoystick,
            SwerveDrive drive,
            ArmAndClimber ss,
            Intake intake) {
        leftJoystick.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);
        rightJoystick.setAllAxisDeadzone(ControlConstants.JOYSTICK_DEADZONE);

        oi.addRule(
                "Reset Gyro",
                leftJoystick.whenButton(InputConstants.BUTTON_RESET_GYRO),
                ONCE,
                Set.of(drive),
                () -> drive.resetGyro());

        oi.addRule(
                "Reset Pos",
                leftJoystick.whenButton(InputConstants.BUTTON_RESET_POS),
                ONCE,
                Set.of(drive),
                () -> drive.resetCurrentPosition());

        oi.addRule(
                "Cross wheels",
                new Conditions.Toggle(rightJoystick.whenButton(InputConstants.BUTTON_CROSS_WHEELS)),
                ONCE_AND_HOLD,
                drive,
                () -> drive.requestStop());

        oi.addRule(
                "Target Shooter",
                leftJoystick.whenButton(InputConstants.BUTTON_TARGET_SHOOTER),
                ONCE_AND_HOLD,
                () -> new DriverShootNow(drive.rotation, ss, intake));

        oi.addRule(
                "Start Shooting",
                rightJoystick.whenButton(InputConstants.BUTTON_START_SHOOTING_PROCEDURE),
                ONCE_AND_HOLD,
                () -> new DriverShootVelocityAndIntake(intake));

        // Moves the robot if there are joystick inputs
        oi.addRule(
                "Translate robot",
                leftJoystick.whenAnyAxisMoved(
                        InputConstants.AXIS_FORWARD_BACKWARD, InputConstants.AXIS_LEFT_RIGHT),
                REPEATEDLY,
                drive.translation,
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

                    return drive.translation.requestFieldOrientedTranslation(
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            leftJoystickX,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER),
                            drivingCoefficient
                                    * curvedJoystickPower(
                                            leftJoystickY,
                                            ControlConstants.TRANSLATIONAL_CURVE_POWER));
                });
        oi.addRule(
                "Rotate robot",
                rightJoystick.whenAxisMoved(InputConstants.AXIS_LEFT_RIGHT),
                REPEATEDLY,
                drive.rotation,
                () -> {
                    // If a button is pressed, drive is just fine adjustment
                    final double drivingCoefficient =
                            rightJoystick.getButton(InputConstants.BUTTON_FINE_DRIVING)
                                    ? ControlConstants.FINE_DRIVING_COEFFICIENT
                                    : 1;
                    // For steer
                    // Negative because left is negative in driver station
                    final double rightJoystickY =
                            -rightJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT)
                                    * ControlConstants.MAX_ROTATIONAL_VELOCITY;

                    return drive.rotation.requestRotationVelocity(
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

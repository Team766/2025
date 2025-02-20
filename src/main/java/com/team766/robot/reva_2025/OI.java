package com.team766.robot.reva_2025;

import com.team766.framework3.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.CoralConstants;
import com.team766.robot.reva_2025.constants.InputConstants;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.*;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;

public class OI extends RuleEngine {

    public class QueuedControl {
        public AlgaeIntake.Level algaeLevel;
        public CoralConstants.ScoreHeight scoreHeight;
    }

    public OI(
            SwerveDrive drive,
            AlgaeIntake algaeIntake,
            Wrist wrist,
            Elevator elevator,
            CoralIntake coralIntake,
            Climber climber) {
        final JoystickReader leftJoystick =
                RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
        final JoystickReader rightJoystick =
                RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);
        final JoystickReader boxopGamepad =
                RobotProvider.instance.getJoystick(InputConstants.BOXOP_GAMEPAD);
        final JoystickReader macropad = 
                RobotProvider.instance.getJoystick(3);
        QueuedControl queuedControl = new QueuedControl();
        queuedControl.algaeLevel = Level.Stow;
        queuedControl.scoreHeight = ScoreHeight.Intake;

        // Add driver control rules here.

        addRules(
                new DriverOI(
                        leftJoystick,
                        rightJoystick,
                        drive,
                        elevator,
                        wrist,
                        coralIntake,
                        algaeIntake,
                        queuedControl));
        addRules(
                new BoxOpOI(
                        boxopGamepad,
                        algaeIntake,
                        elevator,
                        wrist,
                        climber,
                        coralIntake,
                        queuedControl));
        addRules(new DebugOI(macropad, climber, elevator, wrist, algaeIntake, coralIntake));
    }

    @Override
    public Category getLoggerCategory() {
        return Category.OPERATOR_INTERFACE;
    }
}

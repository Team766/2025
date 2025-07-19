package com.team766.robot.mayhem_shooter;

import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;

import com.team766.framework.RuleEngine;
import com.team766.framework.Conditions.LogicalAnd;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.BurroDrive;
import com.team766.robot.mayhem_shooter.mechanisms.*;
import com.team766.robot.mayhem_shooter.procedures.*;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    public OI(BurroDrive drive, Shooter shooter) {
        final JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver control rules here.
        addRule(
            "spin up shooter",
            joystick0.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER),
            ONCE_AND_HOLD,
            shooter,
            () -> shooter.enableShooter()
            );

        addRule(
            "feed into shooter",
            new LogicalAnd(joystick0.whenAxisMoved(InputConstants.GAMEPAD_RIGHT_TRIGGER), whenStatusMatching(Shooter.ShooterStatus.class, s -> s.isAtTargetSpeed())),
            ONCE_AND_HOLD,
            shooter,
            () -> shooter.enableFeeder()
            );
    }
}

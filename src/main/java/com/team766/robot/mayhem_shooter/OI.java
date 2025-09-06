package com.team766.robot.mayhem_shooter;

import static com.team766.framework.RulePersistence.ONCE;
import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;
import static com.team766.framework.RulePersistence.REPEATEDLY;
import com.team766.framework.Conditions.LogicalAnd;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;
import com.team766.robot.common.mechanisms.BurroDrive;
import com.team766.robot.mayhem_shooter.Mechanisms.*;
import com.team766.robot.mayhem_shooter.Procedures.*;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    public OI(Drive drive, Shooter shooter) {
        final JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver control rules here.
        addRule(
            "Joysticks Moved",
            joystick0.whenAnyAxisMoved(0,1),
            REPEATEDLY,
            drive,
            () -> drive.arcadeDrive(joystick0.getAxis(1), joystick0.getAxis(0))
        );
        addRule(
                "spin up shooter",
                joystick0.whenButton(1),
                ONCE_AND_HOLD,
                shooter,
                () -> shooter.enableShooter());

        addRule(
                "feed into shooter",
                joystick0.whenButton(2),
                ONCE_AND_HOLD,
                shooter,
                () -> shooter.enableFeeder());
        addRule(
            "intake",
            joystick0.whenButton(3),
            ONCE_AND_HOLD,
            shooter,
            () -> shooter.setIntakeMotor(0.5)
        );
    }
}

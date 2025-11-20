package com.team766.robot.mayhem_shooter;

import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;
import static com.team766.framework.RulePersistence.ONCE;
import static com.team766.framework.RulePersistence.REPEATEDLY;
import java.util.Set;
import com.team766.framework.RuleEngine;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.robot.mayhem_shooter.mechanisms.*;
import com.team766.robot.mayhem_shooter.procedures.*;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends RuleEngine {
    public OI(Drive drive, Shooter shooter, Vision vision, Lights lights) {
        final JoystickReader joystick0 = RobotProvider.instance.getJoystick(0);
        final JoystickReader joystick1 = RobotProvider.instance.getJoystick(1);
        final JoystickReader joystick2 = RobotProvider.instance.getJoystick(2);

        // Add driver control rules here.
        addRule(
                "Joysticks Moved",
                joystick0.whenAnyAxisMoved(0, 1),
                REPEATEDLY,
                drive,
                () -> { 
                        drive.arcadeDrive(joystick0.getAxis(0), joystick0.getAxis(1));             
                });
        addRule(
                "Intake On",
                joystick0.whenButton(2),
                ONCE,
                shooter,
                () -> shooter.setIntakeMotor(0.5));
        addRule(
                "Feed Ball",
                joystick0.whenButton(1),
                ONCE_AND_HOLD,
                shooter,
                () -> shooter.enableFeeder());
        addRule(
                "Intake Off",
                joystick0.whenButton(3),
                ONCE,
                shooter,
                () -> shooter.setIntakeMotor(0));
        addRule(
                "Shoot Ball for Testing",
                joystick0.whenButton(5),
                ONCE_AND_HOLD,
                shooter,
                () -> shooter.setShooterPower(0.43));
        addRule(
                "Check Kracken Distance",
                joystick0.whenButton(6),
                ONCE,
                Set.of(vision, lights),
                () -> { 
                        lights.setKrakenColor(vision.getDistanceFromKraken());             
                });
        addRule(
                "Shoot Ball Procedure",
                joystick0.whenButton(4),
                ONCE_AND_HOLD,
                () -> new ShootBall(vision, shooter));
    }
}

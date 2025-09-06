package com.team766.robot.mayhem_shooter;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.burro_arm.procedures.DoNothing;    
import com.team766.robot.mayhem_shooter.Mechanisms.*;
import com.team766.robot.mayhem_shooter.Procedures.*;

public class Robot implements RobotConfigurator {
    private Drive drive;
    private Shooter shooter;

    @Override
    public void initializeMechanisms() {
        // Initialize mechanisms here
        drive = new Drive();
        shooter = new Shooter();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, shooter);
    }

    @Override
    public RuleEngine createLights() {
        return new Lights();
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        return new AutonomousMode[] {new AutonomousMode("DONT USE ME", () -> new DoNothing())
            // Add autonomous modes here like this:
            //    new AutonomousMode("NameOfAutonomousMode", () -> new MyAutonomousProcedure()),
            //
            // If your autonomous procedure has constructor arguments, you can
            // define one or more different autonomous modes with it like this:
            //    new AutonomousMode("DriveFast", () -> new DriveStraight(1.0)),
            //    new AutonomousMode("DriveSlow", () -> new DriveStraight(0.4)),

        };
    }
}

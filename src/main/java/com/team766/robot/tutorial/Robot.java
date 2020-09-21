package com.team766.robot.tutorial;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.tutorial.mechanisms.*;
import com.team766.robot.tutorial.procedures.*;

public class Robot implements RobotConfigurator {
    private Drive drive;

    @Override
    public void initializeMechanisms() {
        // Initialize mechanisms here
        drive = new Drive();
    }

    @Override
    public RuleEngine createOI() {
        return new OI();
    }

    @Override
    public RuleEngine createLights() {
        return new Lights();
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        return new AutonomousMode[] {
            // Add autonomous modes here like this:
            //    new AutonomousMode("NameOfAutonomousMode", () -> new MyAutonomousProcedure()),
            //
            // If your autonomous procedure has constructor arguments, you can
            // define one or more different autonomous modes with it like this:
            //    new AutonomousMode("DriveFast", () -> new DriveStraight(1.0)),
            //    new AutonomousMode("DriveSlow", () -> new DriveStraight(0.4)),

            new AutonomousMode("DriveSquare", () -> new DriveSquare(drive)),
            new AutonomousMode("TurnRight", () -> new TurnRight(drive)),
            new AutonomousMode("DriveStraight", () -> new DriveStraight(drive)),
            new AutonomousMode("DoNothing", () -> new DoNothing()),
        };
    }
}

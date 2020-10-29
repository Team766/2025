package com.team766.robot.tutorial;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.tutorial.mechanisms.*;
import com.team766.robot.tutorial.procedures.*;

public class Robot implements RobotConfigurator {
    private Drive drive;
    private Launcher launcher;
    private Intake intake;
    private LineSensors lineSensors;

    @Override
    public void initializeMechanisms() {
        // Initialize mechanisms here
        drive = new Drive();
        launcher = new Launcher();
        intake = new Intake();
        lineSensors = new LineSensors();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, launcher, intake);
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

            new AutonomousMode("FollowLine", () -> new FollowLine(drive)),
            new AutonomousMode("DriveAngle", () -> new TurnAngle(drive)),
            new AutonomousMode("DriveDistance", () -> new DriveDistance(drive)),
            new AutonomousMode("Score5", () -> new Score5(drive, launcher, intake)),
            new AutonomousMode("CollectBalls", () -> new CollectBalls(drive, intake)),
            new AutonomousMode("Launch", () -> new Launch(launcher)),
            new AutonomousMode("DriveSquare", () -> new DriveSquare(drive)),
            new AutonomousMode("TurnRight", () -> new TurnRight(drive)),
            new AutonomousMode("DriveStraight", () -> new DriveStraight(drive)),
            new AutonomousMode("DoNothing", () -> new DoNothing()),
        };
    }
}

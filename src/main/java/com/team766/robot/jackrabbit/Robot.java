package com.team766.robot.jackrabbit;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.jackrabbit.mechanisms.*;

public class Robot implements RobotConfigurator {
    private Drive drive;
    private Collector collector;
    private Spindexer spindexer;
    private Feeder feeder;
    private Turret turret;
    private Hood hood;
    private Shooter shooter;
    private NearsightedLimelight nearsightedLimelight;
    private FarsightedLimelight farsightedLimelight;

    @Override
    public void initializeMechanisms() {
        drive = new Drive();
        collector = new Collector();
        spindexer = new Spindexer();
        feeder = new Feeder();
        turret = new Turret();
        hood = new Hood();
        shooter = new Shooter();
        nearsightedLimelight = new NearsightedLimelight();
        farsightedLimelight = new FarsightedLimelight();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, collector, spindexer, feeder, turret, hood, shooter);
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
        };
    }
}

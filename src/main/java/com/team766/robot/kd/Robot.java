package com.team766.robot.kd;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.kd.mechanisms.*;
import com.team766.robot.kd.procedures.*;

public class Robot implements RobotConfigurator {
    private Shooter shooter;
    private Loader loader;
    private Drive drive;
    private Intake intake;

    @Override
    public void initializeMechanisms() {
        shooter = new Shooter();
        // TODO: Calibrate for optimal loader speed
        loader = new Loader(1);
        // TODO: Calibrate for optimal intake speed
        intake = new Intake(1);
        drive = new Drive();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, shooter, loader, intake);
    }

    @Override
    public RuleEngine createLights() {
        return null;
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

            new AutonomousMode("Auton", () -> new Autonomous(drive)),
        };
    }
}

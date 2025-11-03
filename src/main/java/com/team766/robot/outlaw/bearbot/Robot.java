package com.team766.robot.outlaw.bearbot;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.outlaw.bearbot.mechanisms.Intake;
import com.team766.robot.outlaw.bearbot.mechanisms.Shooter;
import com.team766.robot.outlaw.bearbot.mechanisms.Turret;

public class Robot implements RobotConfigurator {

    private SwerveDrive drive;
    private Intake intake;
    private Shooter shooter;
    private Turret turret;

    @Override
    public void initializeMechanisms() {
        SwerveConfig swerveConfig = new SwerveConfig();
        drive = new SwerveDrive(swerveConfig);
        intake = new Intake();
        shooter = new Shooter();
        turret = new Turret();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive);
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

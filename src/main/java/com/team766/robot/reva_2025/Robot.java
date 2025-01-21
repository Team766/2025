package com.team766.robot.reva_2025;

import com.team766.framework3.AutonomousMode;
import com.team766.framework3.RuleEngine;
import com.team766.hal.RobotConfigurator3;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;

public class Robot implements RobotConfigurator3 {

    private SwerveDrive drive;

    @Override
    public void initializeMechanisms() {
        SwerveConfig swerveConfig = new SwerveConfig();
        drive = new SwerveDrive(swerveConfig);
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

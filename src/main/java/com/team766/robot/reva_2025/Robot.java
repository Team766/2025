package com.team766.robot.reva_2025;

import com.team766.framework3.AutonomousMode;
import com.team766.framework3.RuleEngine;
import com.team766.hal.RobotConfigurator3;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.mechanisms.*;

public class Robot implements RobotConfigurator3 {

    private SwerveDrive drive;
    private AlgaeIntake algaeIntake;
    private Vision vision;
    private Wrist wrist;
    private Elevator elevator;
    private CoralIntake coral;
    private Climber climber;

    @Override
    public void initializeMechanisms() {
        SwerveConfig swerveConfig = new SwerveConfig();
        algaeIntake = new AlgaeIntake();
        drive = new SwerveDrive(swerveConfig);
        // vision = new Vision();
        wrist = new Wrist();
        elevator = new Elevator();
        coral = new CoralIntake();
        climber = new Climber();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, algaeIntake, wrist, elevator, coral, climber);
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

package com.team766.robot.copy_2910;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;

public class Robot implements RobotConfigurator {

    private SwerveDrive drive;

    @Override
    public void initializeMechanisms() {
        SwerveConfig swerveConfig = new SwerveConfig();
        drive = new SwerveDrive(swerveConfig);
    }

    @Override
    public RuleEngine createOI() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createOI'");
    }

    @Override
    public RuleEngine createLights() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createLights'");
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAutonomousModes'");
    }
}

package com.team766.robot.reva_2025;

import com.team766.framework.AutonomousMode;
import com.team766.framework.Procedure;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.mechanisms.*;

public class Robot implements RobotConfigurator {
    public static SwerveDrive drive;
    public static Elevator elevator;

    @Override
    public Procedure createOI() {
        return new OI();
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        return AutonomousModes.AUTONOMOUS_MODES;
    }

    @Override
    public void initializeMechanisms() {
        SwerveConfig config = new SwerveConfig();
        drive = new SwerveDrive(config);
        elevator = new Elevator();
    }
}

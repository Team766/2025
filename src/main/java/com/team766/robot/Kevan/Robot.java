package com.team766.robot.Kevan;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.Kevan.mechanisms.Intake;
import com.team766.robot.Kevan.mechanisms.Shooter;
import com.team766.robot.Kevan.procedures.Autonomous.AutonomousTest;

public class Robot implements RobotConfigurator {

    private Drive drive;
    private Intake intake;
    private Shooter shooter;

    @Override
    public void initializeMechanisms() {
        drive = new Drive();
        intake = new Intake();
        shooter = new Shooter();
    }

    @Override
    public RuleEngine createOI() {
        return new OI_MAYHEM(drive, shooter, intake);
    }

    @Override
    public RuleEngine createLights() {
        return null;
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        return new AutonomousMode[] {
            new AutonomousMode(
                    "Autonomous_For_Mayhem", () -> new AutonomousTest(drive, shooter, intake))
        };
    }
}

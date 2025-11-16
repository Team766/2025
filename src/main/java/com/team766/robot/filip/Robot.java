package com.team766.robot.filip;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.filip.mechanisms.Drive;
import com.team766.robot.filip.mechanisms.Intake;
import com.team766.robot.filip.mechanisms.Shooter;
import com.team766.robot.filip.procedures.Autonomous;

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
                    "Autonomous_For_Mayhem", () -> new Autonomous(drive, shooter, intake))
        };
    }
}

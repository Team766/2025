package com.team766.robot.ArthurDoering;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Intake;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;
//import com.team766.robot.ArthurDoering.procedures.Autons.Autonomous;
import com.team766.robot.ArthurDoering.procedures.Autons.TestAuton;

public class Robot implements RobotConfigurator {
    private Drive drive;
    private Intake intake;
    private Shooter shoot;

    @Override
    public void initializeMechanisms() {
        drive = new Drive();
        intake = new Intake();
        shoot = new Shooter();
    }

    @Override
    public RuleEngine createOI() {
        return new OI_MAYHEM(drive, intake, shoot);
    }

    @Override
    public RuleEngine createLights() {
        return null;
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        return new AutonomousMode[] {
            new AutonomousMode("Autonomous_For_MAyhem", () -> new TestAuton(drive, shoot, intake))
        };
    }
}

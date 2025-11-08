package com.team766.robot.ArthurDoering;

import com.team766.framework.AutonomousMode;
import com.team766.framework.Context;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Intake;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;
import com.team766.robot.ArthurDoering.procedures.Autonomous;

public class Robot implements RobotConfigurator {
    private Drive drive;
    private Intake intake;
    private Shooter shoot;
    private Context context;

    @Override
    public void initializeMechanisms() {
        drive = new Drive();
        intake = new Intake();
        shoot = new Shooter();
    }

    @Override
    public RuleEngine createOI() {
        return new OI_MAYHEM(drive, intake, shoot, context);
    }

    @Override
    public RuleEngine createLights() {
        return null;
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        return new AutonomousMode[] {
            new AutonomousMode("Autonomous_For_MAyhem", () -> new Autonomous(drive, shoot, intake))
        };
    }
}

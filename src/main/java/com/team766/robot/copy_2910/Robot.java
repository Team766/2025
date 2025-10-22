package com.team766.robot.copy_2910;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.copy_2910.mechanisms.Elevator;
import com.team766.robot.copy_2910.mechanisms.Intake;
import com.team766.robot.copy_2910.mechanisms.Shoulder;
import com.team766.robot.copy_2910.mechanisms.Vision;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import com.team766.robot.copy_2910.procedures.CenterL1;
import com.team766.robot.copy_2910.procedures.DriveStraight;
import com.team766.robot.gatorade.Lights;

public class Robot implements RobotConfigurator {

    private SwerveDrive drive;
    private Intake intake;
    private Vision vision;
    // private Climber climber;
    private Elevator elevator;
    private Shoulder shoulder;
    private Wrist wrist;

    @Override
    public void initializeMechanisms() {
        SwerveConfig swerveConfig = new SwerveConfig().withDistanceBetweenWheels(0.533);
        drive = new SwerveDrive(swerveConfig);
        intake = new Intake();
        // climber = new Climber();
        elevator = new Elevator();
        vision = new Vision();
        shoulder = new Shoulder();
        wrist = new Wrist();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, intake, wrist, elevator, shoulder, vision);
    }

    @Override
    public RuleEngine createLights() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'createLights'");
        return new Lights();
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        // TODO Auto-generated method stub
        return new AutonomousMode[] {
            new AutonomousMode("Move", () -> new DriveStraight(drive)),
            new AutonomousMode(
                    "Center L1", () -> new CenterL1(drive, intake, wrist, elevator, shoulder))
        };
        // throw new UnsupportedOperationException("Unimplemented method 'getAutonomousModes'");
    }
}

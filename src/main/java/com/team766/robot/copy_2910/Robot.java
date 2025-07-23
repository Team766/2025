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
import com.team766.robot.copy_2910.procedures.OuttakeCoral;
import com.team766.robot.gatorade.Lights;

public class Robot implements RobotConfigurator {

    private SwerveDrive drive;
    private Intake intake;
    private Elevator elevator;
    private Shoulder shoulder;
    private Vision vision;
    private Wrist wrist;

    @Override
    public void initializeMechanisms() {
        SwerveConfig swerveConfig = new SwerveConfig();
        drive = new SwerveDrive(swerveConfig);
        intake = new Intake();
        elevator = new Elevator();
        shoulder = new Shoulder();
        vision = new Vision();
        wrist = new Wrist();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, intake, wrist, elevator, shoulder, vision);
    }

    @Override
    public RuleEngine createLights() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'createLights'");
        return new Lights();
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        // TODO Auto-generated method stub
        return new AutonomousMode[] { new AutonomousMode("uh", () -> new OuttakeCoral(intake))};
        //throw new UnsupportedOperationException("Unimplemented method 'getAutonomousModes'");
    }
}

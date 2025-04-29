package com.team766.robot.reva;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.mechanisms.Climber;
import com.team766.robot.reva.mechanisms.ForwardApriltagCamera;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.NoteCamera;
import com.team766.robot.reva.mechanisms.Orin;
import com.team766.robot.reva.mechanisms.Shooter;
import com.team766.robot.reva.mechanisms.Shoulder;
import com.team766.robot.reva.procedures.auton_routines.*;

public class Robot implements RobotConfigurator {
    private SwerveDrive drive;
    private Climber climber;
    private Shoulder shoulder;
    private Intake intake;
    private Shooter shooter;
    private ForwardApriltagCamera forwardApriltagCamera;
    private NoteCamera noteCamera;
    private Orin orin;

    @Override
    public void initializeMechanisms() {
        SwerveConfig config = new SwerveConfig();
        drive = new SwerveDrive(config);
        climber = new Climber();
        shoulder = new Shoulder();
        intake = new Intake();
        shooter = new Shooter();
        noteCamera = new NoteCamera();
        forwardApriltagCamera = new ForwardApriltagCamera();
        orin = new Orin();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, climber, shoulder, intake, shooter);
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
            new AutonomousMode(
                    "3p Start Amp, Amp and Center Pieces",
                    () ->
                            new LowerClimbersInParallel(
                                    new ThreePieceAmpSide(drive, shoulder, shooter, intake),
                                    climber)),
            new AutonomousMode(
                    "4p Start Amp, All Close Pieces",
                    () ->
                            new LowerClimbersInParallel(
                                    new FourPieceAmpSide(drive, shoulder, shooter, intake),
                                    climber)),
            new AutonomousMode(
                    "2p Start Source, Bottom Midfield Piece",
                    () ->
                            new LowerClimbersInParallel(
                                    new TwoPieceMidfieldSourceSide(
                                            drive, shoulder, shooter, intake),
                                    climber)),
            new AutonomousMode(
                    "3p Start Amp, Amp and Top Midfield Pieces",
                    () ->
                            new LowerClimbersInParallel(
                                    new ThreePieceMidfieldAmpSide(drive, shoulder, shooter, intake),
                                    climber)),
            new AutonomousMode(
                    "3p Start Center, Amp and Center Pieces",
                    () ->
                            new LowerClimbersInParallel(
                                    new ThreePieceStartCenterTopAndAmp(
                                            drive, shoulder, shooter, intake),
                                    climber)),
            new AutonomousMode(
                    "Just Shoot Amp",
                    () ->
                            new LowerClimbersInParallel(
                                    new JustShootAmp(drive, shoulder, shooter, intake), climber)),
        };
    }
}

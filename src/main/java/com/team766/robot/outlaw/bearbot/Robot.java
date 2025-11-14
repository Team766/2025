package com.team766.robot.outlaw.bearbot;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.outlaw.bearbot.mechanisms.Intake;
import com.team766.robot.outlaw.bearbot.mechanisms.Feeder;
import com.team766.robot.outlaw.bearbot.mechanisms.Shooter;
import com.team766.robot.outlaw.bearbot.mechanisms.Turret;

public class Robot implements RobotConfigurator {

    private SwerveDrive drive;
    private Intake intake;
    private Feeder feeder;
    private Shooter shooter;
    private Turret turret;

    @Override
    public void initializeMechanisms() {
        SwerveConfig swerveConfig = new SwerveConfig();
        drive = new SwerveDrive(swerveConfig);
        intake = new Intake();
        feeder = new Feeder();
        shooter = new Shooter();
        turret = new Turret();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, intake, feeder, shooter, turret);
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
            //
            // Auton Sequence:
            // 1. Robot starts in Harbor at I, M, O (Inside, Middle, or Outside position, where Inside is towards the middle of the field)
            //    The robot is oriented square to the field with the intake facing towards the driver and the turret in the 0 deg position.
            //    The middle of the turret edge is located 2" from the boundary (46" from the driver's side of the field) 
            //    I is 14" from the harbor border on the inside of the field (The intention is for the side of the robot to be 2" from the line).
            //      This is also 82 inches (7' 10") from the closest side border of the field.
            //    M is 48" from the closest side border of the field.
            //    O is 14" from the closest side border of the field.
            // 2. Robot drives 44 inches backwards (shooter toward the target and driving parallel to the field border), deploys the intake, and rotates xx deg as it drives
            //    Recall that the robot coordinate axis system is Forward, Left, Up
            //    xx is -18.4 deg for I, 0 deg for M, and +18.4 deg for O
            // 3. Robot shoots
            // 4. Robot rotates yy deg in place, where yy = -161.6 deg for I, 180 deg for M, and 161.6 deg for O. The intake is now facing the ball.
            // 5. Robot drives forward (still driving parallel to the field border) for 18".
            // 6. Robot rotates zz deg, where zz = -122 deg - phi for I, +90 deg + phi for M, and +112 deg + phi for O.
            //    Phi is an as-yet undetermined angle that the shooter shoots at relative to the robot body.
            //    Simultaneously, the turret shifts to the left side for I and the right side for M and O.
            //    Note that for I the robot is driving toward the near-side side border, whereas for M and O, the robot is driving toward the far-side side border.
            //    Note further that the robot's intake is now pointed away from the tareget by up to 22 + phi deg.
            // 7. The robot drives forward parallel to the driver's side of the field while intaking and shooting.
            //    As it does so it runs the intake and shoots, and it rotates 44 deg as it drives 66 inches along the ball line.
            //    <<Some adjustment is probably needed at this step depending on the angle that the ball shoots>>
            // 8. Robot drives to one of three set spots on the border of the harbor and stops.
            //    The orientation should be the same regardless of the set point with the intake facing roughly toward the far left opposite corner.
            //
            // Pseudocode for the above
            // Define: DriveAndTwist(d, alpha, theta). d is the distance to drive in inches, alpha is the direction of motion (field coordinates), and
            //         theta is the twist to achieve during the drive.
            // Define: Feed(feeder_T). Procedure. Turn on the feeder and try to reach feeder_T.
            // Define: Spinup(omega_T). Procedure. Turn on the Shooter and try to reach omega_T rpm's
            // Define: Shoot(omega_L, omega_H). Boolean function. Signal that the spin lies between omega_L and omega_H 
            // Define: FeedAndShoot(feeder_T, omega_T, omega_L, omega_H). Spin up the shooter and turn on feeder.
            //         Spinup(omega_T)
            //         If Shoot(omega_L, omega_H) then Feed(feeder_T)
            //
            // 1. No action
            // 2. DriveAndTwist(44, 0, xx)
            // 3. FeedAndShoot(feeder_T, omega_T, omega_L, omega_H)
            // 4. DriveAndTwist(0, 0, yy)
            // 5. DriveAndTwist(18, 0, 0)
            // 6. DriveAndTwist(0, 0, zz)
            // 7. FeedAndShoot(feeder_T, omega_T, omega_L, omega_H). DriveAndTwist(66, 90 for red -90 for blue, function of distance driven)
            // 8. DriveAndTwist(...)

        };
    }
}

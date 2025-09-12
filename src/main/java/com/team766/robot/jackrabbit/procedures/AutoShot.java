package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.math.Maths;
import com.team766.robot.jackrabbit.mechanisms.Drive;
import com.team766.robot.jackrabbit.mechanisms.FarsightedLimelight.FarsightStatus;
import com.team766.robot.jackrabbit.mechanisms.Hood;
import com.team766.robot.jackrabbit.mechanisms.Shooter;
import com.team766.robot.jackrabbit.mechanisms.Turret;
import com.team766.robot.jackrabbit.mechanisms.Turret.GyroHeading;

public class AutoShot extends Procedure {
    private record ShootingControlPoint(double distance, double hoodTarget, double shooterTarget) {}

    private static final ShootingControlPoint[] CONTROL_POINTS =
            new ShootingControlPoint[] {
                new ShootingControlPoint(0.0, 60, 5),
                new ShootingControlPoint(3.0, 60, 10),
                new ShootingControlPoint(6.0, 40, 15),
                new ShootingControlPoint(12.0, 30, 30),
            };

    private final Turret turret;
    private final Hood hood;
    private final Shooter shooter;

    public AutoShot(Turret turret, Hood hood, Shooter shooter) {
        this.turret = reserve(turret);
        this.hood = reserve(hood);
        this.shooter = reserve(shooter);
    }

    @Override
    public void run(Context context) {
        for (; ; ) {
            final var pose =
                    waitForStatusMatching(
                                    context,
                                    FarsightStatus.class,
                                    s -> s.poseEstimate().isPresent())
                            .poseEstimate()
                            .get();

            // TODO: This is aiming at the robot's own position. Aim at a target instead.
            final double heading = pose.pose().getTranslation().getAngle().getDegrees();
            final double distance = pose.pose().getTranslation().getNorm();

            final double turretTarget =
                    heading - getStatusOrThrow(Drive.DriveStatus.class).heading();

            final double hoodTarget =
                    Maths.interpolate(
                            CONTROL_POINTS,
                            distance,
                            ShootingControlPoint::distance,
                            ShootingControlPoint::hoodTarget);
            final double shooterTarget =
                    Maths.interpolate(
                            CONTROL_POINTS,
                            distance,
                            ShootingControlPoint::distance,
                            ShootingControlPoint::shooterTarget);

            final GyroHeading targetHeading = turret.setTargetAngle(turretTarget);
            hood.setTargetAngle(hoodTarget);
            shooter.shoot(shooterTarget);

            // TODO: Check for 0 velocity
            if (checkForStatusMatching(
                            Turret.TurretStatus.class, s -> s.isAtGyroHeading(targetHeading))
                    && checkForStatusMatching(Hood.HoodStatus.class, s -> s.isAtAngle(hoodTarget))
                    && checkForStatusMatching(
                            Shooter.ShooterStatus.class, s -> s.isAtSpeed(shooterTarget))) {
                break;
            }

            context.yield();
        }
    }
}

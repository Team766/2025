package com.team766.robot.reva.procedures;

import com.team766.ViSIONbase.AprilTagGeneralCheckedException;
import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.VisionUtil.VisionSpeakerHelper;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import com.team766.robot.reva.mechanisms.Shoulder;
import edu.wpi.first.math.geometry.Rotation2d;

public class RotateAndShootNow extends Procedure {

    private final SwerveDrive drive;
    private final Shoulder shoulder;
    private final Shooter shooter;
    private final Intake intake;

    private final VisionSpeakerHelper visionSpeakerHelper;

    public RotateAndShootNow(SwerveDrive drive, Shoulder shoulder, Shooter shooter, Intake intake) {
        this.drive = reserve(drive);
        this.shoulder = reserve(shoulder);
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
        visionSpeakerHelper = new VisionSpeakerHelper();
    }

    // TODO: ADD LED COMMANDS BASED ON EXCEPTIONS
    public void run(Context context) {
        drive.stopDrive();

        // double power;
        double armAngle;
        Rotation2d heading;

        visionSpeakerHelper.update();

        try {
            // power = visionSpeakerHelper.getShooterPower();
            armAngle = visionSpeakerHelper.getArmAngle();
            heading = visionSpeakerHelper.getHeadingToTarget();
        } catch (AprilTagGeneralCheckedException e) {
            LoggerExceptionUtils.logException(e);
            return;
        }

        shoulder.rotate(armAngle);
        drive.controlFieldOrientedWithRotationTarget(0, 0, heading);
        // shooter.shoot(power);

        waitForStatusMatchingOrTimeout(
                context, Shoulder.ShoulderStatus.class, s -> s.isNearTo(armAngle), 0.5);
        waitForStatusMatchingOrTimeout(
                context, SwerveDrive.DriveStatus.class, s -> s.isAtRotationHeading(heading), 3.0);
        drive.stopDrive();

        context.runSync(new ShootVelocityAndIntake(shooter, intake));
    }
}

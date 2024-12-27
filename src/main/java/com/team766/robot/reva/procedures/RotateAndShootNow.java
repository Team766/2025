package com.team766.robot.reva.procedures;

import static com.team766.framework3.Conditions.waitForRequestOrTimeout;

import com.team766.ViSIONbase.AprilTagGeneralCheckedException;
import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.VisionUtil.VisionSpeakerHelper;
import com.team766.robot.reva.mechanisms.ArmAndClimber;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import edu.wpi.first.math.geometry.Rotation2d;

public class RotateAndShootNow extends Procedure {

    private final SwerveDrive.Rotation driveRotation;
    private final ArmAndClimber superstructure;
    private final Shooter shooter;
    private final Intake intake;

    private final VisionSpeakerHelper visionSpeakerHelper;

    public RotateAndShootNow(
            SwerveDrive.Rotation driveRotation,
            ArmAndClimber superstructure,
            Shooter shooter,
            Intake intake) {
        this.driveRotation = reserve(driveRotation);
        this.superstructure = reserve(superstructure);
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
        visionSpeakerHelper = new VisionSpeakerHelper();
    }

    // TODO: ADD LED COMMANDS BASED ON EXCEPTIONS
    public void run(Context context) {
        driveRotation.requestStop();

        // double power;
        double armTarget;
        Rotation2d headingTarget;

        visionSpeakerHelper.update();

        try {
            // power = visionSpeakerHelper.getShooterPower();
            armTarget = visionSpeakerHelper.getArmAngle();
            headingTarget = visionSpeakerHelper.getHeadingToTarget();
        } catch (AprilTagGeneralCheckedException e) {
            LoggerExceptionUtils.logException(e);
            return;
        }

        var armRequest = superstructure.requestShoulderPosition(armTarget);
        var headingRequest = driveRotation.requestRotationTarget(headingTarget);
        // shooter.requestSpeed(power);

        waitForRequestOrTimeout(context, armRequest, 0.5);
        waitForRequestOrTimeout(context, headingRequest, 3.0);
        driveRotation.requestStop();

        context.runSync(new ShootVelocityAndIntake(shooter, intake));
    }
}

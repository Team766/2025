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

public class NoRotateShootNow extends Procedure {

    private final SwerveDrive drive;
    private final ArmAndClimber superstructure;
    private final Shooter shooter;
    private final Intake intake;
    private final VisionSpeakerHelper visionSpeakerHelper;
    private final boolean amp;

    public NoRotateShootNow(
            boolean amp,
            SwerveDrive drive,
            ArmAndClimber superstructure,
            Shooter shooter,
            Intake intake) {
        this.drive = reserve(drive);
        this.superstructure = reserve(superstructure);
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
        this.amp = amp;
        visionSpeakerHelper = new VisionSpeakerHelper();
    }

    public void run(Context context) {
        if (!amp) {
            drive.requestStop();

            double speedTarget;
            double armTarget;

            visionSpeakerHelper.update();

            try {
                speedTarget = visionSpeakerHelper.getShooterPower();
                armTarget = visionSpeakerHelper.getArmAngle();
            } catch (AprilTagGeneralCheckedException e) {
                LoggerExceptionUtils.logException(e);
                return;
            }

            var armRequest = superstructure.requestShoulderPosition(armTarget);

            // start shooting now while waiting for shoulder, stopped in ShootVelocityAndIntake
            shooter.requestSpeed(speedTarget);

            waitForRequestOrTimeout(context, armRequest, 0.5);

            context.runSync(new ShootVelocityAndIntake(speedTarget, shooter, intake));

        } else {
            // Robot.shooter.shoot(3000);
            // Robot.shoulder.rotate(ShoulderPosition.AMP);

            // context.waitFor(Robot.shoulder::isFinished);

            context.runSync(new ShootVelocityAndIntake(3000, shooter, intake));
        }
    }
}

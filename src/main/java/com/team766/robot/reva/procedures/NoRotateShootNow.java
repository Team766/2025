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

public class NoRotateShootNow extends Procedure {

    private final SwerveDrive drive;
    private final Shoulder shoulder;
    private final Shooter shooter;
    private final Intake intake;
    private final VisionSpeakerHelper visionSpeakerHelper;
    private final boolean amp;

    public NoRotateShootNow(
            boolean amp, SwerveDrive drive, Shoulder shoulder, Shooter shooter, Intake intake) {
        this.drive = reserve(drive);
        this.shoulder = reserve(shoulder);
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
        this.amp = amp;
        visionSpeakerHelper = new VisionSpeakerHelper();
    }

    public void run(Context context) {
        if (!amp) {
            drive.stopDrive();

            double power;
            double armAngle;

            visionSpeakerHelper.update();

            try {
                power = visionSpeakerHelper.getShooterPower();
                armAngle = visionSpeakerHelper.getArmAngle();
            } catch (AprilTagGeneralCheckedException e) {
                LoggerExceptionUtils.logException(e);
                return;
            }

            shoulder.rotate(armAngle);

            // start shooting now while waiting for shoulder, stopped in ShootVelocityAndIntake
            shooter.shoot(power);

            waitForStatusMatchingOrTimeout(
                    context, Shoulder.ShoulderStatus.class, s -> s.isNearTo(armAngle), 0.5);

            context.runSync(new ShootVelocityAndIntake(power, shooter, intake));

        } else {
            // Robot.shooter.shoot(3000);
            // Robot.shoulder.rotate(ShoulderPosition.AMP);

            // context.waitFor(Robot.shoulder::isFinished);

            context.runSync(new ShootVelocityAndIntake(3000, shooter, intake));
        }
    }
}

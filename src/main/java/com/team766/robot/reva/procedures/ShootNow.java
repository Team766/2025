package com.team766.robot.reva.procedures;

import static com.team766.framework3.Conditions.waitForRequestOrTimeout;
import static com.team766.framework3.Conditions.waitForValueOrTimeout;

import com.team766.ViSIONbase.AprilTagGeneralCheckedException;
import com.team766.framework3.Context;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva.VisionUtil.VisionPIDProcedure;
import com.team766.robot.reva.mechanisms.ArmAndClimber;
import com.team766.robot.reva.mechanisms.ForwardApriltagCamera;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.Optional;

public class ShootNow extends VisionPIDProcedure {

    private final SwerveDrive drive;
    private final ArmAndClimber superstructure;
    private final Shooter shooter;
    private final Intake intake;

    public ShootNow(
            SwerveDrive drive, ArmAndClimber superstructure, Shooter shooter, Intake intake) {
        this.drive = reserve(drive);
        this.superstructure = reserve(superstructure);
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
    }

    // TODO: ADD LED COMMANDS BASED ON EXCEPTIONS
    public void run(Context context) {
        publishStatus(new ShootingProcedureStatus(ShootingProcedureStatus.Status.RUNNING));
        drive.requestStop();

        Optional<Transform3d> maybeToUse =
                waitForValueOrTimeout(context, () -> getTransform3dOfRobotToTag(), 1.0);
        if (!maybeToUse.isPresent()) {
            log(Severity.ERROR, "No tag info available");
            return;
        }
        Transform3d toUse = maybeToUse.orElseThrow();

        double x = toUse.getX();
        double y = toUse.getY();

        anglePID.setSetpoint(0);

        double distanceOfRobotToTag =
                Math.sqrt(Math.pow(toUse.getX(), 2) + Math.pow(toUse.getY(), 2));

        if (distanceOfRobotToTag
                > VisionPIDProcedure.scoringPositions
                        .get(VisionPIDProcedure.scoringPositions.size() - 1)
                        .distanceFromCenterApriltag()) {
            publishStatus(new ShootingProcedureStatus(ShootingProcedureStatus.Status.OUT_OF_RANGE));
            return;
        }
        double speedTarget;
        double armTarget;
        try {
            speedTarget = VisionPIDProcedure.getBestPowerToUse(distanceOfRobotToTag);
            armTarget = VisionPIDProcedure.getBestArmAngleToUse(distanceOfRobotToTag);
        } catch (AprilTagGeneralCheckedException e) {
            LoggerExceptionUtils.logException(e);
            return;
        }

        shooter.requestSpeed(speedTarget);

        var armRequest = superstructure.requestShoulderPosition(armTarget);

        double angle = Math.atan2(y, x);

        anglePID.calculate(angle);

        while (Math.abs(anglePID.getOutput()) > 0.075) {
            context.yield();

            // SmartDashboard.putNumber("[ANGLE PID OUTPUT]", anglePID.getOutput());
            // SmartDashboard.putNumber("[ANGLE PID ROTATION]", angle);

            maybeToUse = getTransform3dOfRobotToTag();
            if (!maybeToUse.isPresent()) {
                continue;
            }
            toUse = maybeToUse.orElseThrow();

            y = toUse.getY();
            x = toUse.getX();

            angle = Math.atan2(y, x);

            anglePID.calculate(angle);

            drive.rotation.requestRotationVelocity(-anglePID.getOutput());
        }

        drive.requestStop();

        // SmartDashboard.putNumber("[ANGLE PID OUTPUT]", anglePID.getOutput());
        // SmartDashboard.putNumber("[ANGLE PID ROTATION]", angle);

        waitForRequestOrTimeout(context, armRequest, 1);

        publishStatus(new ShootingProcedureStatus(ShootingProcedureStatus.Status.FINISHED));
        context.runSync(new ShootVelocityAndIntake(speedTarget, shooter, intake));
    }

    private Optional<Transform3d> getTransform3dOfRobotToTag() {
        return getStatus(ForwardApriltagCamera.ApriltagCameraStatus.class)
                .flatMap(s -> s.speakerTagTransform());
    }
}
